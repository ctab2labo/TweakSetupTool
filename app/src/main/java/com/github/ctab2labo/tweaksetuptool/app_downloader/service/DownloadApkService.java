package com.github.ctab2labo.tweaksetuptool.app_downloader.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import com.github.ctab2labo.tweaksetuptool.Common;
import com.github.ctab2labo.tweaksetuptool.R;
import com.github.ctab2labo.tweaksetuptool.app_downloader.activity.AppDownloaderActivity;
import com.github.ctab2labo.tweaksetuptool.app_downloader.adapter.DownloadedFile;
import com.github.ctab2labo.tweaksetuptool.app_downloader.json.AppPackage;
import com.github.ctab2labo.tweaksetuptool.app_downloader.task.FileDownloadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// 非同期でファイルをダウンロードするサービス
public class DownloadApkService extends Service {
    private final IBinder binder = new DownloadApkServiceBinder();
    public static final String EXTRA_DOWNLOAD_PACKAGE = "extra_download_package";
    private final int INTENT_REQUEST_DOWNLOADING = 1;
    private final int INTENT_REQUEST_DOWNLOADED = 2;

    private ArrayList<AppPackage> appPackageList;
    private int count;
    private ArrayList<File> downloadedFileList;

    // いろいろなリスナー
    private List<OnProgressUpdateListener> onProgressUpdateListeners = new ArrayList<>();
    private List<OnDownloadedListener> onDownloadedListeners = new ArrayList<>();
    private List<OnCompletedListener> onCompletedListeners = new ArrayList<>();
    private List<OnDownloadFailedListener> onDownloadFailedListeners = new ArrayList<>();

    private FileDownloadTask task;

    private Notification.Builder notification;
    private int progressMax;
    private int percent;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNewTask(intent);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void createNewTask(Intent intent) {
        appPackageList = (ArrayList<AppPackage>) intent.getSerializableExtra(EXTRA_DOWNLOAD_PACKAGE);

        // 通知を設定
        progressMax = appPackageList.size() * 100;
        notification = new Notification.Builder(this);
        notification.setSmallIcon(android.R.drawable.stat_sys_download);
        notification.setContentTitle(getString(R.string.notify_downloading_title, 0));
        notification.setContentText(getString(R.string.text_download_app, appPackageList.get(0).name, 1, appPackageList.size()));
        notification.setProgress(100, 0, false);
        Intent intent2 = new Intent(this, AppDownloaderActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, INTENT_REQUEST_DOWNLOADING, intent2, PendingIntent.FLAG_CANCEL_CURRENT);
        notification.setContentIntent(pendingIntent);
        startForeground(Common.AppDownloader.NOTIFICATION_ID_DOWNLOADING, notification.build());

        count = -1;
        downloadedFileList = new ArrayList<>();
        downloadNextApp();
    }

    private int getPercent(int progress) {
        progress = progress * 100 / progressMax;
        if (progress > 100) progress = 100;
        return progress;
    }

    private void downloadNextApp() {
        count++;

        if (count < appPackageList.size()) { // まだまだダウンロードするものがあるなら
            // ファイルリストにファイルを新規作成
            downloadedFileList.add(new File(Common.EXTERNAL_SAVE_DIRECTORY, String.valueOf(count) + ".apk"));

            // タスクを初期化
            task = new FileDownloadTask(appPackageList.get(count).url, downloadedFileList.get(count));
            task.setOnCompletedListener(new FileDownloadTask.OnCompletedListener() {
                @Override
                public void onCompleted(Exception e) {
                    if (e == null) {
                        downloadNextApp();
                        downloaded(count);
                    } else {
                        downloadFailed(e);
                    }
                }
            });
            task.setUpdateListener(new FileDownloadTask.OnProgressUpdateListener() {
                @Override
                public void onUpdate(int i) {
                    progressUpdate(count, i);
                }
            });
            task.execute();
        } else {
            allCompleted(downloadedFileList);
        }
    }

    public void cancel() {
        if (task != null) {
            task.cancel(false);
        }
        stopSelf();
    }

    public ArrayList<AppPackage> getAppPackageList() {
        return appPackageList;
    }

    public static void startDownloadService(Context context, ArrayList<AppPackage> appPackageList) {
        Intent intent = new Intent(context, DownloadApkService.class);
        intent.putExtra(EXTRA_DOWNLOAD_PACKAGE, appPackageList);
        context.startService(intent);
    }

    public static boolean bindDownloadService(Context context, ServiceConnection serviceConnection) {
        if (isActiveService(context)) {
            Intent intent = new Intent(context, DownloadApkService.class);
            return context.bindService(intent, serviceConnection, 0);
        } else {
            return false;
        }
    }

    public static boolean isActiveService(Context context)
    {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServicesInfo = activityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : runningServicesInfo)
        {
            if (runningServiceInfo.service.getClassName().equals(DownloadApkService.class.getName()))
            {
                return true;
            }
        }
        return false;
    }

    private void progressUpdate(int index, int progress) {
        int percent = getPercent(index * 100 + progress);
        if (percent != this.percent) {
            this.percent = percent;
            notification.setProgress(100, this.percent, false);
            notification.setContentText(getString(R.string.text_download_app, appPackageList.get(index).name, index + 1, appPackageList.size()));
            notification.setContentTitle(getString(R.string.notify_downloading_title, this.percent));
            startForeground(Common.AppDownloader.NOTIFICATION_ID_DOWNLOADING, notification.build());
        }
        for (OnProgressUpdateListener listener : onProgressUpdateListeners) {
            listener.onProgressUpdate(index, progress);
        }
    }

    private void downloaded(int index) {
        for (OnDownloadedListener listener : onDownloadedListeners) {
            listener.onDownloaded(index);
        }
    }

    private void allCompleted(ArrayList<File> downloadedFileList) {
        boolean flag = false;
        for (OnCompletedListener listener : onCompletedListeners) {
            if (listener.onCompleted(downloadedFileList)) {
                flag = true;
            }
        }
        stopForeground(true);
        if (! flag) { // 指定がないなら通知をする
            Notification.Builder builder = new Notification.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentTitle(getString(R.string.notify_downloaded_title));
            builder.setContentText(getString(R.string.notify_downloaded_text));
            Intent intent = new Intent(this, AppDownloaderActivity.class);
            intent.putExtra(AppDownloaderActivity.EXTRA_MODE, AppDownloaderActivity.MODE_SHOW_INSTALL_APK_FRAGMENT);
            intent.putExtra(AppDownloaderActivity.EXTRA_DOWNLOADED_FILES, DownloadedFile.fileWithAppPackageListToDownloadedFileList(appPackageList, downloadedFileList));
            PendingIntent pendingIntent = PendingIntent.getActivity(this, INTENT_REQUEST_DOWNLOADED, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            builder.setContentIntent(pendingIntent);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(Common.AppDownloader.NOTIFICATION_ID_DOWNLOADED, builder.build());
        }
        stopSelf();
    }

    private void downloadFailed(Exception e) {
        for (OnDownloadFailedListener listener : onDownloadFailedListeners) {
            listener.onDownloadFailed(e);
        }

        for(File file : downloadedFileList) { // ダウンロードしたファイルの削除
            file.delete();
        }
        stopForeground(true);
        stopSelf();
    }

    // 進行状態のリスナー
    public interface OnProgressUpdateListener {
        void onProgressUpdate(int index, int progress);
    }

    // 一つ一つのファイルをダウンロードしたときのリスナー
    public interface OnDownloadedListener {
        void onDownloaded(int index);
    }

    // すべてのファイルをダウンロードしたときのリスナー
    public interface OnCompletedListener {
        /**
         * @return trueにすると、コンプリート通知を送信しません。
         */
        boolean onCompleted(ArrayList<File> downloadedFiles);
    }

    // エラーが出たときのリスナー
    public interface OnDownloadFailedListener {
        void onDownloadFailed(Exception e);
    }

    public void addOnProgressUpdateListener(OnProgressUpdateListener onProgressUpdateListener) {
        this.onProgressUpdateListeners.add(onProgressUpdateListener);
    }

    public void addOnDownloadedListener(OnDownloadedListener onDownloadedListener) {
        this.onDownloadedListeners.add(onDownloadedListener);
    }

    public void addOnCompletedListener(OnCompletedListener onCompletedListener) {
        this.onCompletedListeners.add(onCompletedListener);
    }

    public void addOnDownloadFailedListener(OnDownloadFailedListener onDownloadFailedListener) {
        this.onDownloadFailedListeners.add(onDownloadFailedListener);
    }

    public void removeOnProgressUpdateListener(OnProgressUpdateListener onProgressUpdateListener) {
        this.onProgressUpdateListeners.remove(onProgressUpdateListener);
    }

    public void removeOnDownloadedListener(OnDownloadedListener onDownloadedListener) {
        this.onDownloadedListeners.remove(onDownloadedListener);
    }

    public void removeOnCompletedListener(OnCompletedListener onCompletedListener) {
        this.onCompletedListeners.remove(onCompletedListener);
    }

    public void removeOnDownloadFailedListener(OnDownloadFailedListener onDownloadFailedListener) {
        this.onDownloadFailedListeners.remove(onDownloadFailedListener);
    }

    public class DownloadApkServiceBinder extends Binder {
        public DownloadApkService getService() {
            return DownloadApkService.this;
        }
    }
}

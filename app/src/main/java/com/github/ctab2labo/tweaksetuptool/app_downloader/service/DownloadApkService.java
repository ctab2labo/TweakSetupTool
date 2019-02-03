package com.github.ctab2labo.tweaksetuptool.app_downloader.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.github.ctab2labo.tweaksetuptool.Common;
import com.github.ctab2labo.tweaksetuptool.app_downloader.json.AppPackage;
import com.github.ctab2labo.tweaksetuptool.app_downloader.task.FileDownloadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// 非同期でファイルをダウンロードするサービス
public class DownloadApkService extends Service {
    private final IBinder binder = new DownloadApkServiceBinder();
    public static final String EXTRA_DOWNLOAD_PACKAGE = "extra_download_package";

    private ArrayList<AppPackage> appPackageList;
    private int count;
    private ArrayList<File> downloadedFileList;

    // いろいろなリスナー
    private List<OnProgressUpdateListener> onProgressUpdateListeners = new ArrayList<>();
    private List<OnDownloadedListener> onDownloadedListeners = new ArrayList<>();
    private List<OnCompletedListener> onCompletedListeners = new ArrayList<>();
    private List<OnDownloadFailedListener> onDownloadFailedListeners = new ArrayList<>();

    private FileDownloadTask task;

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
/* 開発中
        // 通知を設定
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(android.R.drawable.stat_sys_download);
        builder.setContentTitle(getString(R.string.notify_download_title));
        builder.setContentText("");
*/

        count = -1;
        downloadedFileList = new ArrayList<>();
        downloadNextApp();
    }

    private void downloadNextApp() {
        count++;

        if (count < appPackageList.size()) { // まだまだダウンロードするものがあるなら
            // ファイルリストにファイルを新規作成
            downloadedFileList.add(new File(Common.SAVE_DIRECTORY, String.valueOf(count) + ".apk"));

            // タスクを初期化
            task = new FileDownloadTask(appPackageList.get(count).url, downloadedFileList.get(count));
            task.setOnCompletedListener(new FileDownloadTask.OnCompletedListener() {
                @Override
                public void onCompleted(Exception bool) {
                    if (bool == null) {
                        downloadNextApp();
                        downloaded(count);
                    } else {
                        downloadFailed(bool);
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
            task.cancel(true);
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
            return context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
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
        Log.d(Common.TAG, "DownloadApkService:progressUpdate");
        for (OnProgressUpdateListener listener : onProgressUpdateListeners) {
            listener.onProgressUpdate(index, progress);
        }
    }

    private void downloaded(int index) {
        Log.d(Common.TAG, "DownloadApkService:downloaded");
        for (OnDownloadedListener listener : onDownloadedListeners) {
            listener.onDownloaded(index);
        }
        stopSelf();
    }

    private void allCompleted(ArrayList<File> downloadedFileList) {
        Log.d(Common.TAG, "DownloadApkService:allCompleted");
        for (OnCompletedListener listener : onCompletedListeners) {
            listener.onCompleted(downloadedFileList);
        }
    }

    private void downloadFailed(Exception e) {
        for (OnDownloadFailedListener listener : onDownloadFailedListeners) {
            Log.d(Common.TAG, "DownloadApkService:downloadFailed");
            listener.onDownloadFailed(e);
        }
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
        void onCompleted(ArrayList<File> downloadedFiles);
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

    public class DownloadApkServiceBinder extends Binder {
        public DownloadApkService getService() {
            return DownloadApkService.this;
        }
    }
}

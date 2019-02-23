package com.github.ctab2labo.tweaksetuptool.app_downloader.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.github.ctab2labo.tweaksetuptool.Common;
import com.github.ctab2labo.tweaksetuptool.R;
import com.github.ctab2labo.tweaksetuptool.app_downloader.activity.AppDownloaderActivity;
import com.github.ctab2labo.tweaksetuptool.app_downloader.json.DeliveryList;
import com.github.ctab2labo.tweaksetuptool.app_downloader.task.FileDownloadTask;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;

public class UpdateCheckService extends Service {
    private final int REQUEST_CODE_NOTIFY = 1;

    private final Handler handler = new Handler();
    private File listFile;
    private final Runnable checkRunnable = new Runnable() {
        @Override
        public void run() {
            FileDownloadTask task = new FileDownloadTask(getString(R.string.url_list), listFile);
            task.setOnCompletedListener(onCompletedListener);
            task.execute();
        }
    };

    private final FileDownloadTask.OnCompletedListener onCompletedListener = new FileDownloadTask.OnCompletedListener() {
        @Override
        public void onCompleted(Exception e) {
            DeliveryList deliveryList = null;
            if (e == null) { // 正常にダウンロードできたら読み込んでみる
                try {
                    // ファイルを読み込む
                    FileInputStream inputStream = new FileInputStream(listFile);
                    String listString = new String(Common.readAll(inputStream));
                    Gson gson = new Gson();
                    deliveryList = gson.fromJson(listString, DeliveryList.class);
                } catch (Exception e2) {
                    e = e2;
                }
            }

            if (deliveryList != null) {
                // 保存されていた値を取得
                SharedPreferences sp = getSharedPreferences(Common.AppDownloader.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
                int latest_downloaded_version = sp.getInt(Common.AppDownloader.KEY_LATEST_LIST_VERSION, 0);
                if (deliveryList.list_version == latest_downloaded_version) { // バージョンチェックして、大丈夫だったら
                    stopSelf(); // サービスを終了
                } else {
                    // 通知を表示して知らせる。
                    Notification.Builder notification = new Notification.Builder(UpdateCheckService.this);
                    notification.setContentTitle(getString(R.string.notify_check_list_update_title));
                    notification.setContentText(getString(R.string.notify_check_list_update_text));
                    notification.setSmallIcon(R.mipmap.ic_launcher); // アイコン募集中
                    Intent intent = new Intent(UpdateCheckService.this, AppDownloaderActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(UpdateCheckService.this, REQUEST_CODE_NOTIFY, intent, PendingIntent.FLAG_ONE_SHOT);
                    notification.setContentIntent(pendingIntent);
                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.notify(Common.AppDownloader.NOTIFICATION_ID_LIST_UPDATE, notification.build());

                    // そして終了
                    stopSelf();
                }
            } else { // 失敗したら
                Log.e(Common.TAG, "UpdateCheckService", e);
                handler.postDelayed(checkRunnable, 10 * 1000); // 10秒後に再試行
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        listFile = new File(getFilesDir(), "delivery_list.json");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 実行
        handler.post(checkRunnable);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(Common.TAG, "UpdateCheckService:Unsupported bind.");
        return null;
    }
}

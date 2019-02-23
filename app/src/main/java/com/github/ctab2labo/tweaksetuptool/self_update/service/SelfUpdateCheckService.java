package com.github.ctab2labo.tweaksetuptool.self_update.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.github.ctab2labo.tweaksetuptool.Common;
import com.github.ctab2labo.tweaksetuptool.R;
import com.github.ctab2labo.tweaksetuptool.app_downloader.task.FileDownloadTask;
import com.github.ctab2labo.tweaksetuptool.self_update.activity.SelfUpdateActivity;
import com.github.ctab2labo.tweaksetuptool.self_update.json.AppInfo;

import java.io.File;
import java.io.FileInputStream;

public class SelfUpdateCheckService extends Service {
    private final int REQUEST_CODE_NOTIFY = 1;

    private final Handler handler = new Handler();
    private File jsonFile;
    private final Runnable checkRunnable = new Runnable() {
        @Override
        public void run() {
            FileDownloadTask task = new FileDownloadTask(getString(R.string.url_list), jsonFile);
            task.setOnCompletedListener(onCompletedListener);
            task.execute();
        }
    };

    private final FileDownloadTask.OnCompletedListener onCompletedListener = new FileDownloadTask.OnCompletedListener() {
        @Override
        public void onCompleted(Exception e) {
            AppInfo appInfo = null;
            if (e == null) { // 正常にダウンロードできた場合
                try { // よみこむ
                    FileInputStream inputStream = new FileInputStream(jsonFile);
                    String jsonString = new String(Common.readAll(inputStream));
                    appInfo = AppInfo.fromJson(jsonString);
                } catch (Exception e1) {
                    e = e1;
                }
            }

            if (appInfo != null) { // 正常に読み込めた場合
                if (appInfo.app_version != Common.SelfUpdate.PUBLIC_APP_VERSION) { // 異なるバージョンなら通知
                    Notification.Builder notification = new Notification.Builder(SelfUpdateCheckService.this);
                    notification.setContentTitle(getText(R.string.notify_self_check_title));
                    notification.setContentText(getText(R.string.notify_self_check_text));
                    notification.setSmallIcon(R.mipmap.ic_launcher);
                    Intent intent = new Intent(SelfUpdateCheckService.this, SelfUpdateActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(SelfUpdateCheckService.this, REQUEST_CODE_NOTIFY, intent, PendingIntent.FLAG_ONE_SHOT);
                    notification.setContentIntent(pendingIntent);
                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.notify(Common.SelfUpdate.NOTIFICATION_ID_SELF_UPDATE, notification.build());
                }
                stopSelf(); // サービスを終了
            } else { // 失敗したら
                Log.e(Common.TAG, "SelfUpdateCheckService:Download failed.", e);
                handler.postDelayed(checkRunnable, 10 * 1000); // 10秒後に再試行
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        jsonFile = new File(getFilesDir(), "appInfo.json");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.post(checkRunnable); // 実行
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(Common.TAG, "SelfUpdateCheckService:Unsupported bind.");
        return null;
    }
}

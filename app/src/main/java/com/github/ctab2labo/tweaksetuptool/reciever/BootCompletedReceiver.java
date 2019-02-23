package com.github.ctab2labo.tweaksetuptool.reciever;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.github.ctab2labo.tweaksetuptool.Common;
import com.github.ctab2labo.tweaksetuptool.app_downloader.service.UpdateCheckService;
import com.github.ctab2labo.tweaksetuptool.self_update.service.SelfUpdateCheckService;
import com.github.ctab2labo.tweaksetuptool.system_ui_tweak.service.KeepService;

public class BootCompletedReceiver extends BroadcastReceiver {
    private final int INTENT_REQUEST_APP_DOWNLOADER_UPDATE_CHECK_SERVICE = 3;
    private final int INTENT_REQUEST_SELF_UPDATE_CHECK_SERVICE = 4;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) { // インテントのフィルター
            // KeepService
            SharedPreferences sp = context.getSharedPreferences(Common.SystemUITweak.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
            // サービスが有効であれば起動
            if(sp.getBoolean(Common.SystemUITweak.KEY_ENABLED_KEEP_SERVICE, false)) {
                context.startService(new Intent(context, KeepService.class)); // サービスを起動
            }

            // UpdateCheckService
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent1 = new Intent(context, UpdateCheckService.class);
            PendingIntent pendingIntent = PendingIntent.getService(context, INTENT_REQUEST_APP_DOWNLOADER_UPDATE_CHECK_SERVICE, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
            // 12時間周期の定期処理をセット
            am.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), 12 * 60 * 60 * 1000, pendingIntent);

            // SelfUpdateCheckService
            intent1 = new Intent(context, SelfUpdateCheckService.class);
            pendingIntent = PendingIntent.getService(context, INTENT_REQUEST_SELF_UPDATE_CHECK_SERVICE, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
            // 12時間周期の定期処理をセット
            am.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), 12 * 60 * 60 * 1000, pendingIntent);
        }
    }
}

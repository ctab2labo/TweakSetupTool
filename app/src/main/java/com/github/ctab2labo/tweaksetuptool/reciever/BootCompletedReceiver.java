package com.github.ctab2labo.tweaksetuptool.reciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.ctab2labo.tweaksetuptool.system_ui_tweak.service.KeepService;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) { // インテントのフィルター
            context.startService(new Intent(context, KeepService.class)); // サービスを起動
        }
    }
}

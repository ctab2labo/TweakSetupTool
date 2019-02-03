package com.github.ctab2labo.tweaksetuptool.reciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.ctab2labo.tweaksetuptool.system_ui_tweak.service.KeepService;

public class UpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) { // レシーバーを確認
            context.startService(new Intent(context, KeepService.class)); // サービスを起動
        }
    }
}

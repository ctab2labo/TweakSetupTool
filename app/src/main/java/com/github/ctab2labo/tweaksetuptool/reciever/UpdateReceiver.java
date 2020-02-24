package com.github.ctab2labo.tweaksetuptool.reciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class UpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
    }
}

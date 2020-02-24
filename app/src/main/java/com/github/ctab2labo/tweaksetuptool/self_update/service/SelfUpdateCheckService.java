package com.github.ctab2labo.tweaksetuptool.self_update.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.github.ctab2labo.tweaksetuptool.Util;

public class SelfUpdateCheckService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(Util.TAG, "SelfUpdateCheckService:Unsupported bind.");
        return null;
    }
}

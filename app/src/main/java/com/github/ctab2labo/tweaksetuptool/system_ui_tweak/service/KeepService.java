package com.github.ctab2labo.tweaksetuptool.system_ui_tweak.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

import com.github.ctab2labo.tweaksetuptool.Common;

import jp.co.benesse.dcha.dchaservice.IDchaService;

public class KeepService extends Service {
    private static final String TAG = "KeepService";

    // ContentObserver関係を宣言
    private ContentResolver resolver;
    private final String hideNavigationBarString = "hide_navigation_bar";
    private final Uri contentHideNavigationBar = Settings.System.getUriFor(hideNavigationBarString);
    private final ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            try {
                if (Settings.System.getInt(resolver, hideNavigationBarString) == 1) {
                    // もし、ナビゲーションバーを隠されたら、表示
                    if (mDchaService == null) { // 同期できていなかったら再同期
                        Log.e(TAG, "Error DchaService is null.");

                        // サービスの再同期
                        if (! bindDchaService()) {
                            // 失敗したら、終了
                            stopSelf();
                        }
                    } else {
                        try {
                            mDchaService.hideNavigationBar(false);
                        } catch (RemoteException e) {
                            Log.e(TAG, "Error DchaService can't hideNavigationBar.", e);
                            // 失敗したら、終了
                            stopSelf();
                        }
                    }
                }
            } catch (Settings.SettingNotFoundException e) {
                Log.e(TAG, "Settings.SettingsNotFoundException", e);
            }
        }
    };
    private ContentObserver observer;

    private IDchaService mDchaService = null;
    private final ServiceConnection dchaServiceConnetion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // オブサーバーを消去
            if (observer != null) {
                resolver.unregisterContentObserver(observer);
                observer = null;
            }
            Log.d(TAG, "Binded DchaService.");
            mDchaService = IDchaService.Stub.asInterface(iBinder);
            try {
                // 同期したら、試しにテスト
                mDchaService.hideNavigationBar(false);

                // 成功したら、コンテントオブサーバーを登録
                resolver = getContentResolver();
                observer = mObserver;
                resolver.registerContentObserver(contentHideNavigationBar, false, observer);
            } catch (RemoteException e) {
                Log.e(TAG, "Testing is Error.");
                stopSelf();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mDchaService = null;
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sp = getSharedPreferences(Common.SHARED_PREFERENCE_KEY, MODE_PRIVATE);
        // サービスが有効ではない場合は停止。
        if(!sp.getBoolean(Common.KEY_ENABLED_KEEP_SERVICE, false)) {
            Log.d(TAG, "Disabled.");
            stopSelf();
            return START_STICKY;
        }

        // サービスの同期
        if (! bindDchaService()) {
            // 失敗したら、終了
            stopSelf();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Stopping Service...");
        if (observer != null) {
            resolver.unregisterContentObserver(observer);
            observer = null;
        }
        if (mDchaService != null) {
            unbindService(dchaServiceConnetion);
            mDchaService = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "Not Supported Bind.");
        return null;
    }

    private boolean bindDchaService() {
        if (mDchaService != null) {
            unbindService(dchaServiceConnetion);
            mDchaService = null;
        }
        Log.d(TAG, "Binding DchaService.");
        Intent intent = new Intent("jp.co.benesse.dcha.dchaservice.DchaService");
        intent.setPackage("jp.co.benesse.dcha.dchaservice");
        return bindService(intent, dchaServiceConnetion, Context.BIND_AUTO_CREATE);
    }
}

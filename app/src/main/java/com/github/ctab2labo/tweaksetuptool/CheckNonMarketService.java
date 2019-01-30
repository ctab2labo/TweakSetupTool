package com.github.ctab2labo.tweaksetuptool;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;

// 「提供元不明のアプリ」が許可されたらアクティビティに戻るサービス
public class CheckNonMarketService extends Service {
    private ContentResolver resolver;
    private final String stringNonMarket = Settings.Secure.INSTALL_NON_MARKET_APPS;
    private final Uri contentNonMarket = Settings.Secure.getUriFor(stringNonMarket);
    private final ContentObserver observer = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            try {
                if (Settings.Secure.getInt(resolver, stringNonMarket) == 1) {
                    Intent intent = new Intent(CheckNonMarketService.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    stopSelf();
                }
            } catch (Settings.SettingNotFoundException e) { }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        resolver = getContentResolver();
        resolver.registerContentObserver(contentNonMarket,false, observer);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        resolver.unregisterContentObserver(observer);
        super.onDestroy();
    }
}

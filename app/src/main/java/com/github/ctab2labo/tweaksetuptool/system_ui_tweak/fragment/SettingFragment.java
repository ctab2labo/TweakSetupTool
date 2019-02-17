package com.github.ctab2labo.tweaksetuptool.system_ui_tweak.fragment;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.github.ctab2labo.tweaksetuptool.Common;
import com.github.ctab2labo.tweaksetuptool.R;
import com.github.ctab2labo.tweaksetuptool.system_ui_tweak.service.KeepService;

import jp.co.benesse.dcha.dchaservice.IDchaService;

import static com.github.ctab2labo.tweaksetuptool.Common.TAG;

public class SettingFragment extends PreferenceFragment {
    private static final int FLAG_NOTHING = 0;
    private static final int FLAG_SET_DCHA_STATE_0 = 1;
    private static final int FLAG_SET_DCHA_STATE_3 = 2;
    private static final int FLAG_HIDE_NAVIGATION_BAR = 3;
    private static final int FLAG_VIEW_NAVIGATION_BAR = 4;


    // コンテント取得用
    private ContentResolver resolver;
    private final String hideNavigationBarString = "hide_navigation_bar";
    private final Uri contentHideNavigationBar = Settings.System.getUriFor(hideNavigationBarString);
    private final String dchaStateString = "dcha_state";
    private final Uri contentDchaState = Settings.System.getUriFor(dchaStateString);

    private ContentObserver observerState = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            try {
                switchDchaState.setChecked(Settings.System.getInt(resolver, dchaStateString) != 0);
            } catch (Settings.SettingNotFoundException e) {
                Log.e(TAG, "observerState:SettingNotFoundException",e);
            }
        }
    };

    private ContentObserver observerHide = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            try {
                switchHideBar.setChecked(Settings.System.getInt(resolver, hideNavigationBarString) == 1);
            } catch (Settings.SettingNotFoundException e) {
                Log.e(TAG, "observerHide:SettingNotFoundException",e);
            }
        }
    };
    private boolean isObserberStateEnable = false;
    private boolean isObserberHideEnable = false;
    private boolean canUseThisApp = false;

    private SwitchPreference switchDchaState;
    private SwitchPreference switchHideBar;
    private SwitchPreference switchEnableService;
    private IDchaService mDchaService;

    private int connectionFlag = 0;
    private ServiceConnection dchaServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mDchaService = IDchaService.Stub.asInterface(iBinder);

            try {
                switch(SettingFragment.this.connectionFlag) {
                    case FLAG_NOTHING:
                        Log.e(TAG, "Why flag is nothing?");
                        break;
                    case FLAG_SET_DCHA_STATE_0:
                        mDchaService.setSetupStatus(0);
                        break;
                    case FLAG_SET_DCHA_STATE_3:
                        mDchaService.setSetupStatus(3);
                        break;
                    case FLAG_HIDE_NAVIGATION_BAR:
                        mDchaService.hideNavigationBar(true);
                        break;
                    case FLAG_VIEW_NAVIGATION_BAR:
                        mDchaService.hideNavigationBar(false);
                        break;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
                switch (SettingFragment.this.connectionFlag) {
                    case FLAG_NOTHING:
                        break;
                    case FLAG_SET_DCHA_STATE_0:
                    case FLAG_SET_DCHA_STATE_3:
                        try {
                            switchDchaState.setChecked(Settings.System.getInt(resolver, dchaStateString) != 0);
                        } catch (Settings.SettingNotFoundException e1) {
                            Log.e(TAG, "dchaServiceConnection:SettingNotFoundException", e);
                        }
                        break;
                    case FLAG_HIDE_NAVIGATION_BAR:
                    case FLAG_VIEW_NAVIGATION_BAR:
                        try {
                            switchHideBar.setChecked(Settings.System.getInt(resolver, hideNavigationBarString) == 1);
                        } catch (Settings.SettingNotFoundException e1) {
                            Log.e(TAG, "dchaServiceConnection:SettingNotFoundException", e);
                        }
                        break;

                }
                Toast.makeText(getActivity(), R.string.toast_failed, Toast.LENGTH_LONG).show();
            }

            // サービスから切断
            SettingFragment.this.getActivity().unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mDchaService = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pre_ui_tweak_setting);

        // 読み込む。
        switchDchaState = (SwitchPreference) findPreference(getString(R.string.pre_set_dcha_state));
        switchHideBar = (SwitchPreference) findPreference(getString(R.string.pre_hide_navigationbar));
        switchEnableService = (SwitchPreference) findPreference(getString(R.string.pre_enable_service));

        resolver = getActivity().getContentResolver();

        try {
            switchDchaState.setChecked(Settings.System.getInt(resolver, dchaStateString) != 0);
            switchHideBar.setChecked(Settings.System.getInt(resolver, hideNavigationBarString) == 1);
            SharedPreferences sp = getActivity().getSharedPreferences(Common.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
            switchEnableService.setChecked(sp.getBoolean(Common.KEY_ENABLED_KEEP_SERVICE, false));
            canUseThisApp = true;

            // オブサーバーを有効化
            isObserberStateEnable = true;
            resolver.registerContentObserver(contentDchaState, false, observerState);
            isObserberHideEnable = true;
            resolver.registerContentObserver(contentHideNavigationBar, false, observerHide);

            // リスナーを有効化
            switchDchaState.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    boolean bool;
                    if ((boolean) o) {
                        bool = bindDchaService(FLAG_SET_DCHA_STATE_3);
                    } else {
                        bool =  bindDchaService(FLAG_SET_DCHA_STATE_0);
                    }
                    if(! bool) {
                        Toast.makeText(getActivity(), R.string.toast_failed, Toast.LENGTH_LONG).show();
                    }
                    return bool;
                }
            });
            switchHideBar.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    boolean bool;
                    if ((boolean) o) {
                        bool = bindDchaService(FLAG_HIDE_NAVIGATION_BAR);
                    } else {
                        bool =  bindDchaService(FLAG_VIEW_NAVIGATION_BAR);
                    }
                    if(! bool) {
                        Toast.makeText(getActivity(), R.string.toast_failed, Toast.LENGTH_LONG).show();
                    }
                    return bool;
                }
            });
            switchEnableService.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {// とりあえず、値を保存
                    SharedPreferences sp = getActivity().getSharedPreferences(Common.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
                    SharedPreferences.Editor spe = sp.edit();
                    spe.putBoolean(Common.KEY_ENABLED_KEEP_SERVICE, (boolean)o);
                    spe.apply();
                    if ((boolean) o) {// オンなら起動
                        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
                        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
                            if (KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                                // 実行中なら起動しない
                                return true;
                            }
                        }
                        getActivity().startService(new Intent(getActivity(), KeepService.class));
                    } else {
                        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
                        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
                            if (KeepService.class.getName().equals(serviceInfo.service.getClassName())) {
                                // 実行中なら終了
                                getActivity().stopService(new Intent(getActivity(), KeepService.class));
                                return true;
                            }
                        }
                    }
                    return true;
                }
            });
        } catch (Settings.SettingNotFoundException e) {
            // 設定がないということはチャレンジパッド２ではないということ。
            // ダイアログを表示して終了
            Log.e(TAG, "SettingNotFoundException", e);
            switchDchaState.setEnabled(false);
            switchHideBar.setEnabled(false);
            switchEnableService.setEnabled(false);
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.cannot_use_dialog_title)
                    .setMessage(R.string.cannot_use_dialog_message)
                    .setPositiveButton(R.string.finish, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SettingFragment.this.getActivity().finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // オブサーバーが有効ならば無効化
        if(isObserberStateEnable) {
            resolver.unregisterContentObserver(observerState);
            isObserberStateEnable = false;
        }
        if(isObserberHideEnable) {
            resolver.unregisterContentObserver(observerHide);
            isObserberHideEnable = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // アプリが使えるならオブサーバーを有効化
        if(canUseThisApp) {
            isObserberStateEnable = true;
            resolver.registerContentObserver(contentDchaState, false, observerState);
            isObserberHideEnable = true;
            resolver.registerContentObserver(contentHideNavigationBar, false, observerHide);
        }
        try {
            switchDchaState.setChecked(Settings.System.getInt(resolver, dchaStateString) != 0);
            switchHideBar.setChecked(Settings.System.getInt(resolver, hideNavigationBarString) == 1);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "onResume:SettingNotFoundException", e);
        }
        SharedPreferences sp = getActivity().getSharedPreferences(Common.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        switchEnableService.setChecked(sp.getBoolean(Common.KEY_ENABLED_KEEP_SERVICE, false));
    }

    private boolean bindDchaService(int flag) {
        connectionFlag = flag;
        Intent intent = new Intent("jp.co.benesse.dcha.dchaservice.DchaService");
        intent.setPackage("jp.co.benesse.dcha.dchaservice");
        return getActivity().bindService(intent, dchaServiceConnection,Context.BIND_AUTO_CREATE);
    }
}

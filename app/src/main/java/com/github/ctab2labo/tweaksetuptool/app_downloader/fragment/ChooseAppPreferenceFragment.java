package com.github.ctab2labo.tweaksetuptool.app_downloader.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.github.ctab2labo.tweaksetuptool.Common;
import com.github.ctab2labo.tweaksetuptool.R;
import com.github.ctab2labo.tweaksetuptool.app_downloader.json.AppPackage;

import java.util.ArrayList;

// 渡されたアプリリストを一覧表示し、選択するフラグメント
public class ChooseAppPreferenceFragment extends PreferenceFragment {
    public static final String EXTRA_APP_PACKAGE_LIST = "extra_app_package_list";
    private PreferenceScreen screen;
    private Intent intent;
    private ArrayList<AppPackage> appPackageList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pre_choose_app);

        screen = (PreferenceScreen) findPreference(getString(R.string.pre_app_packages));

        appPackageList = (ArrayList<AppPackage>) getArguments().getSerializable(EXTRA_APP_PACKAGE_LIST);
        // ないとは思うが、もしリストがnullだったらからのリストを作ってしのぐ。
        if (appPackageList == null) {
            Log.d(Common.TAG, "ChooseAppPreferenceFragment:appPackageList is null.");
            appPackageList = new ArrayList<>();
        }

        for (AppPackage appPackage : appPackageList) {
            addAppPackage(appPackage);
        }
    }

    // スクリーンに追加する
    private void addAppPackage(AppPackage appPackage) {
        CheckBoxPreference preference = new CheckBoxPreference(getActivity());
        preference.setTitle(appPackage.name);// もしかしたら説明文がないかもしれないのでチェックする
        if (appPackage.summary != null) preference.setSummary(appPackage.summary);
        preference.setChecked(true);
        screen.addPreference(preference);
    }

    // 結果を聞き出すとき用のメソッド
    public ArrayList<AppPackage> getCheckedAppPackages() {
        ArrayList<AppPackage> tmpList = new ArrayList<>();
        for (int i=0;i<screen.getPreferenceCount();i++) {
            if (((CheckBoxPreference) screen.getPreference(i)).isChecked()) { // チェックがオンなら
                tmpList.add(appPackageList.get(i));
            }
        }
        return tmpList;
    }
}

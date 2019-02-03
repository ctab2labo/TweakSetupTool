package com.github.ctab2labo.tweaksetuptool.menu.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.github.ctab2labo.tweaksetuptool.R;
import com.github.ctab2labo.tweaksetuptool.VersionInfoActivity;
import com.github.ctab2labo.tweaksetuptool.app_downloader.activity.AppDownloaderActivity;
import com.github.ctab2labo.tweaksetuptool.system_ui_tweak.activity.SettingActivity;

public class MenuFragment extends PreferenceFragment {
    private Preference preAppDownload;
    private Preference preUiTweak;
    private Preference preVersionInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pre_menu);

        preAppDownload = findPreference(getString(R.string.pre_app_download));
        preUiTweak = findPreference(getString(R.string.pre_system_ui_tweak));
        preVersionInfo = findPreference(getString(R.string.pre_versioninfo));

        preAppDownload.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), AppDownloaderActivity.class));
                return true;
            }
        });
        preUiTweak.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), SettingActivity.class));
                return true;
            }
        });
        preVersionInfo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), VersionInfoActivity.class));
                return true;
            }
        });
    }
}

package com.github.ctab2labo.tweaksetuptool.app_downloader.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.ctab2labo.tweaksetuptool.Common;
import com.github.ctab2labo.tweaksetuptool.R;
import com.github.ctab2labo.tweaksetuptool.app_downloader.dialog.FileDownloadProgressDialog;
import com.github.ctab2labo.tweaksetuptool.app_downloader.fragment.ChooseAppFragment;
import com.github.ctab2labo.tweaksetuptool.app_downloader.fragment.DownloadApkFragment;
import com.github.ctab2labo.tweaksetuptool.app_downloader.json.AppPackage;
import com.github.ctab2labo.tweaksetuptool.app_downloader.json.DeliveryList;
import com.github.ctab2labo.tweaksetuptool.app_downloader.service.CheckNonMarketService;
import com.github.ctab2labo.tweaksetuptool.app_downloader.service.DownloadApkService;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class AppDownloaderActivity extends Activity{
    public static final String EXTRA_MODE = "extra_mode";
    public static final int MODE_NONE = 0;
    public static final int MODE_SHOW_DOWNLOAD_APK_FRAGMENT = 1;

    private FragmentTransaction transaction;
    private String otherException;

    private File deliveryListFile;

    private boolean finishedCreate = false;

    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloader);

        deliveryListFile = new File(getFilesDir(), "delivery_list.json");

        // 一時ディレクトリの作成
        if (! Common.SAVE_DIRECTORY.exists()) {
            Common.SAVE_DIRECTORY.mkdir();
        }
        setTitle(R.string.title_downloader);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        if (DownloadApkService.isActiveService(this)) mode = MODE_SHOW_DOWNLOAD_APK_FRAGMENT;
        // あとはonResumeに任せる
    }

    private void startOfMode(int mode) {
        mode = getIntent().getIntExtra(EXTRA_MODE, MODE_NONE);
        switch (mode) {
            case MODE_NONE:
                if (isNonMarketEnabled()) { // 提供元不明のアプリがオンなら
                    finishedCreate = true;
                    //　ダイアログを表示してダウンロード
                    new FileDownloadProgressDialog.Builder(this, getString(R.string.url_list), deliveryListFile)
                            .setTitle(R.string.dialog_download_list_title)
                            .setMessage(R.string.dialog_download_list_message)
                            .setOnCompletedListener(onCompletedListener)
                            .setCancelable(false)
                            .show();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.dialog_disable_non_market_title)
                            .setMessage(R.string.dialog_disable_non_market_message)
                            .setPositiveButton(R.string.dialog_disable_non_market_positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(AppDownloaderActivity.this, CheckNonMarketService.class);
                                    startService(intent);
                                    intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    Toast.makeText(AppDownloaderActivity.this, R.string.toast_enable_non_market, Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    AppDownloaderActivity.this.finish();
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
                break;
            case MODE_SHOW_DOWNLOAD_APK_FRAGMENT:
                transaction = getFragmentManager().beginTransaction();
                // すぐにファイルたちを表示する
                DownloadApkFragment fragment = new DownloadApkFragment();
                transaction.replace(R.id.layout_downloader, fragment);
                transaction.commit();
                break;
        }
    }

    private boolean isNonMarketEnabled() {
        int i = 0;
        try {
            i = Settings.Secure.getInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS);
        } catch (Settings.SettingNotFoundException e) {}
        return i == 1;
    }

    private final FileDownloadProgressDialog.OnCompletedListener onCompletedListener = new FileDownloadProgressDialog.OnCompletedListener() {
        @Override
        public void onCompleted(Exception e) {
            DeliveryList deliveryList = null;
            if (e == null) { // 正常にダウンロードできたら読み込んでみる
                try {
                    // ファイルを読み込む
                    FileInputStream inputStream = new FileInputStream(deliveryListFile);
                    String listString = new String(Common.readAll(inputStream));
                    Gson gson = new Gson();
                    deliveryList = gson.fromJson(listString, DeliveryList.class);
                } catch (Exception e2) {
                    e = e2;
                }
            }

            if(e == null) {// 正常に読み込めた場合
                transaction = getFragmentManager().beginTransaction();
                ChooseAppFragment fragment = new ChooseAppFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable(ChooseAppFragment.EXTRA_APP_PACKAGE_LIST, (ArrayList<AppPackage>)deliveryList.app_list);
                fragment.setArguments(bundle);
                fragment.setOnButtonClickListener(onButtonClickListener);
                transaction.replace(R.id.layout_downloader, fragment);
                transaction.commit();
            } else { // 失敗した場合
                otherException = e.toString(); //get exception string
                if(otherException.equals("java.net.ProtocolException: unexpected end of stream")) {
                    new AlertDialog.Builder(AppDownloaderActivity.this)
                            .setTitle(R.string.dialog_download_list_title)
                            .setMessage(getString(R.string.dialog_error_list_message, "ProtocolException"))
                            .setPositiveButton(R.string.ok, null)
                            .show();
                } else {
                    new AlertDialog.Builder(AppDownloaderActivity.this)
                            .setTitle(R.string.dialog_download_list_title)
                            .setMessage(getString(R.string.dialog_download_list_exception) + "\n" + otherException)
                            .setPositiveButton(R.string.ok, null)
                            .show();
                }
            }
        }
    };

    private final ChooseAppFragment.OnButtonClickListener onButtonClickListener = new ChooseAppFragment.OnButtonClickListener() {
        @Override
        public void onButtonClick(ArrayList<AppPackage> appPackageList) {
            transaction = getFragmentManager().beginTransaction();
            DownloadApkFragment fragment = new DownloadApkFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(DownloadApkFragment.EXTRA_APP_PACKAGE_LIST, appPackageList);
            fragment.setArguments(bundle);
            transaction.replace(R.id.layout_downloader, fragment);
            transaction.commit();
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if((! finishedCreate) && mode == MODE_NONE) {
            startOfMode(mode);
        }
    }
}

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
import com.github.ctab2labo.tweaksetuptool.app_downloader.dialog.ListDownloadProgressDialog;
import com.github.ctab2labo.tweaksetuptool.app_downloader.fragment.ChooseAppFragment;
import com.github.ctab2labo.tweaksetuptool.app_downloader.fragment.DownloadApkFragment;
import com.github.ctab2labo.tweaksetuptool.app_downloader.json.AppPackage;
import com.github.ctab2labo.tweaksetuptool.app_downloader.json.DeliveryList;
import com.github.ctab2labo.tweaksetuptool.app_downloader.service.CheckNonMarketService;

import java.util.ArrayList;

public class AppDownloaderActivity extends Activity{
    public static final String EXTRA_MODE = "extra_mode";
    public static final int MODE_NONE = 0;
    public static final int MODE_SHOW_DOWNLOAD_APK_FRAGMENT = 1;

    private FragmentTransaction transaction;
    private String otherException;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloader);
        // 一時ディレクトリの作成
        if (! Common.SAVE_DIRECTORY.exists()) {
            Common.SAVE_DIRECTORY.mkdir();
        }
        setTitle(R.string.title_downloader);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        switch (getIntent().getIntExtra(EXTRA_MODE, MODE_NONE)) {
            case MODE_NONE:
                if (isNonMarketEnabled()) { // 提供元不明のアプリがオンなら
                    //　ダイアログを表示してダウンロード
                    new ListDownloadProgressDialog.Builder(this)
                            .setTitle(R.string.dialog_download_list_title)
                            .setMessage(R.string.dialog_download_list_message)
                            .setOnCompletedListener(onCompletedListener)
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

    private final ListDownloadProgressDialog.OnCompletedListener onCompletedListener = new ListDownloadProgressDialog.OnCompletedListener() {
        @Override
        public void onCompleted(DeliveryList deliveryList, Exception e) {
            if(deliveryList != null) {// 正常にダウンロードできた場合
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

/*
    private int tmpInt;
    private int appsCount = -1;
    private AppPackage[] downloadPackages;
    private ArrayList<File> downloadedFiles;

    private void downloadNextApp() {
        // カウントアップ
        appsCount++;
        if (appsCount < downloadPackages.length) { // まだまだダウンロードするものがある場合
            // 表示の更新
            titleView.setText(getString(R.string.text_download_app, downloadPackages[appsCount].name, appsCount + 1, downloadPackages.length));

            tmpInt = appsCount * 100;
            downloadedFiles.add(new File(Common.SAVE_DIRECTORY, String.valueOf(appsCount) + ".apk"));
            FileDownloadTask task = new FileDownloadTask(downloadPackages[appsCount].url, downloadedFiles.get(appsCount));
            task.setUpdateListener(new FileDownloadTask.OnProgressUpdateListener() {
                @Override
                public void onUpdate(int i) {
                    setBar(tmpInt + i);
                }
            });
            task.setOnCompletedListener(new FileDownloadTask.OnCompletedListener() {
                @Override
                public void onCompleted(Exception e) {
                    if (e == null) {
                        downloadNextApp();
                    } else {
                        new AlertDialog.Builder(AppDownloaderActivity.this)
                                .setTitle(R.string.dialog_download_list)
                                .setMessage(getString(R.string.dialog_download_list_exception) + "\n" + e.toString())
                                .setPositiveButton(R.string.ok, null)
                                .show();
                        reset();
                    }
                }
            });
            task.execute();
        } else {
            appsCount = -1;
            setProgressMax(downloadedFiles.size());
            installNextApp();
        }
    }

    private void installNextApp() {
        appsCount++;
        setBar(appsCount);
        if (appsCount < downloadedFiles.size()) { // まだまだインストールするものがある場合
            titleView.setText(getString(R.string.text_install_app, downloadedFiles.get(appsCount).getName(), appsCount, downloadedFiles.size()));
            showAppInstall(downloadedFiles.get(appsCount));
        } else {
            appsCount = -1;
            reset();
            deleteAllDownloadedFiles();
            titleView.setText(R.string.text_all_success);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_APP_INSTALL) {
            if (resultCode == RESULT_OK) {
                installNextApp();
            } else if (resultCode == RESULT_FIRST_USER) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_cancel_install_title)
                        .setMessage(R.string.dialog_cancel_install_message)
                        .setPositiveButton(R.string.dialog_cancel_positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                installNextApp();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                reset();
                            }
                        })
                        .show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_cancel_install_title)
                        .setMessage(R.string.dialog_cancel_install_message)
                        .setPositiveButton(R.string.dialog_cancel_positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                installNextApp();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                reset();
                            }
                        })
                        .show();
            }
        } else if (requestCode == REQUEST_CHOOSE_APP) {
            if (resultCode == RESULT_OK) { // Step2 アプリのダウンロード
                // 色々初期化したら始まり。
                downloadPackages = (AppPackage[]) data.getSerializableExtra(Common.EXTRA_APP_PACKAGES);
                setProgressMax(downloadPackages.length * 100);
                downloadedFiles = new ArrayList<>();
                appsCount = -1;
                downloadNextApp();
            } else { // でなければ終了
                reset();
            }
        }
    }

    private void showAppInstall(File file) {
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setData(Uri.fromFile(file));
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);

        startActivityForResult(intent, REQUEST_APP_INSTALL);
        Toast toast = Toast.makeText(this, R.string.toast_install_app, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 150);
        toast.show();
    }

    private void deleteAllDownloadedFiles() {
        // ダウンロードしたファイルをすべて削除
        for (File file : downloadedFiles) {
            file.delete();
            if (! file.exists()) {
                downloadedFiles.remove(file);
            }
        }
    }*/
}

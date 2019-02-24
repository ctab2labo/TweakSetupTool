package com.github.ctab2labo.tweaksetuptool.self_update.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.github.ctab2labo.tweaksetuptool.Common;
import com.github.ctab2labo.tweaksetuptool.R;
import com.github.ctab2labo.tweaksetuptool.app_downloader.dialog.FileDownloadProgressDialog;
import com.github.ctab2labo.tweaksetuptool.self_update.json.AppInfo;

import java.io.File;
import java.io.FileInputStream;

public class SelfUpdateActivity extends Activity {
    private File jsonFile;
    private AppInfo appInfo;
    private File updateApk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        jsonFile = new File(getFilesDir(), "appInfo.json");
        updateApk = new File(Common.EXTERNAL_SAVE_DIRECTORY, "selfUpdate.apk");

        // 一時ディレクトリの作成
        if (! Common.EXTERNAL_SAVE_DIRECTORY.exists()) {
            Common.EXTERNAL_SAVE_DIRECTORY.mkdir();
        }

        // 通知を消去
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(Common.SelfUpdate.NOTIFICATION_ID_SELF_UPDATE);

        // アップデートをチェック
        FileDownloadProgressDialog.Builder builder = new FileDownloadProgressDialog.Builder(this, getString(R.string.self_check_url), jsonFile);
        builder.setTitle(R.string.dialog_self_check_title);
        builder.setMessage(R.string.dialog_self_check_text);
        builder.setCancelable(false);
        builder.setOnCompletedListener(onJsonDownloadCompletedListener);
        builder.show();
    }

    private final FileDownloadProgressDialog.OnCompletedListener onJsonDownloadCompletedListener = new FileDownloadProgressDialog.OnCompletedListener() {
        @Override
        public void onCompleted(Exception e) {
            appInfo = null;
            if (e == null) { // 正常にダウンロードできた場合
                try { // よみこむ
                    FileInputStream inputStream = new FileInputStream(jsonFile);
                    String jsonString = new String(Common.readAll(inputStream));
                    appInfo = AppInfo.fromJson(jsonString);
                } catch (Exception e1) {
                    e = e1;
                }
            }

            if (appInfo != null) { // 正常に読み込めた場合
                if (appInfo.app_version != Common.SelfUpdate.PUBLIC_APP_VERSION) {
                    new AlertDialog.Builder(SelfUpdateActivity.this)
                            .setTitle(R.string.dialog_self_check_found_update_title)
                            .setMessage(getString(R.string.dialog_self_check_found_update_text, appInfo.summary))
                            .setPositiveButton(R.string.ok, foundUpdatePositiveListener)
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // アクティビティを閉じる
                                    SelfUpdateActivity.this.finish();
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    // アクティビティを閉じる
                                    SelfUpdateActivity.this.finish();
                                }
                            })
                            .show();
                } else {
                    new AlertDialog.Builder(SelfUpdateActivity.this)
                            .setTitle(R.string.dialog_self_check_non_update_title)
                            .setMessage(getString(R.string.dialog_self_check_non_update_text))
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // アクティビティを閉じる
                                    SelfUpdateActivity.this.finish();
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    // アクティビティを閉じる
                                    SelfUpdateActivity.this.finish();
                                }
                            })
                            .show();
                }
            } else { // 失敗したら
                Log.e(Common.TAG, "SelfUpdateCheckService:Download failed.", e);
                new AlertDialog.Builder(SelfUpdateActivity.this)
                        .setTitle(R.string.dialog_self_check_exception_title)
                        .setMessage(getString(R.string.dialog_self_check_exception_text, e.toString()))
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // アクティビティを閉じる
                                SelfUpdateActivity.this.finish();
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                // アクティビティを閉じる
                                SelfUpdateActivity.this.finish();
                            }
                        })
                        .show();
            }
        }
    };

    private DialogInterface.OnClickListener foundUpdatePositiveListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            FileDownloadProgressDialog.Builder builder = new FileDownloadProgressDialog.Builder(SelfUpdateActivity.this, appInfo.app_dlurl, updateApk);
            builder.setTitle(R.string.dialog_self_check_download_apk_title);
            builder.setMessage(R.string.dialog_self_check_download_apk_text);
            builder.setCancelable(true);
            builder.setOnCompletedListener(onApkDownloadCompletedListener);
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    SelfUpdateActivity.this.finish();
                }
            });
            builder.show();
        }
    };

    private FileDownloadProgressDialog.OnCompletedListener onApkDownloadCompletedListener = new FileDownloadProgressDialog.OnCompletedListener() {
        @Override
        public void onCompleted(Exception e) {
            if (e != null) { // ダウンロードに失敗したらエラーを出して終了。
                Log.e(Common.TAG, "SelfUpdateCheckService:Download failed.", e);
                new AlertDialog.Builder(SelfUpdateActivity.this)
                        .setTitle(R.string.dialog_self_check_download_apk_exception_title)
                        .setMessage(getString(R.string.dialog_self_check_download_apk_exception_text, e.toString()))
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // アクティビティを閉じる
                                SelfUpdateActivity.this.finish();
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                // アクティビティを閉じる
                                SelfUpdateActivity.this.finish();
                            }
                        })
                        .show();
            } else {
                // インストール画面を表示しながら終了
                Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                intent.setData(Uri.fromFile(updateApk));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Toast toast = Toast.makeText(SelfUpdateActivity.this, R.string.toast_install_app, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 150);
                toast.show();
                SelfUpdateActivity.this.finish();
            }
        }
    };
}

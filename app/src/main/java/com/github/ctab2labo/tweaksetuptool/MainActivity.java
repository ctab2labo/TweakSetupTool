package com.github.ctab2labo.tweaksetuptool;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ctab2labo.tweaksetuptool.dialog.ChoseListDialog;
import com.github.ctab2labo.tweaksetuptool.json.AppPackage;
import com.github.ctab2labo.tweaksetuptool.json.DeliveryList;
import com.github.ctab2labo.tweaksetuptool.task.FileDownloadTask;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener{
    private final String DIRECTORY_NAME = "com.github.ctab2labo.tweaksetuptool";
    private final int REQUEST_APP_INSTALL = 732;

    private TextView titleView;
    private ProgressBar progress;
    private TextView progressView;
    private Button button;
    private File deliverylistFile;

    private int progressMax = 100;

    private int tmpInt;
    private int appsCount = -1;
    private AppPackage[] downloadPackages;
    private List<File> downloadedFiles;
    private File saveDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titleView = (TextView) findViewById(R.id.title_view);
        progress = (ProgressBar) findViewById(R.id.progress);
        progressView = (TextView) findViewById(R.id.progress_view);
        button = (Button) findViewById(R.id.button);

        deliverylistFile = new File(getFilesDir(), "deliveryList.json");
        button.setOnClickListener(this);
        progress.setMax(100);

        saveDirectory = new File(Environment.getExternalStorageDirectory(), DIRECTORY_NAME);
        if (! saveDirectory.exists()) {
            saveDirectory.mkdir();
        }
    }

    @Override
    public void onClick(View view) {
        button.setEnabled(false);
        titleView.setText(getString(R.string.text_download_list));
        setBar(0);
        setProgressMax(100);
        FileDownloadTask task = new FileDownloadTask(getString(R.string.url_list), deliverylistFile);
        task.setSuccessListner(urlListSuccessListner);
        task.setUpdateListner(new FileDownloadTask.OnProgressUpdateListner() {
            @Override
            public void onUpdate(int i) {
                setBar(i);
            }
        });
        task.execute();
    }

    // Step1
    private final FileDownloadTask.OnSuccessListner urlListSuccessListner = new FileDownloadTask.OnSuccessListner() {
        @Override
        public void onSuccess(Exception e) {
            if (e == null) {
                String listString;
                try {
                    // ファイルを読み込む
                    FileInputStream inputStream = new FileInputStream(deliverylistFile);
                    listString = new String(readAll(inputStream));
                } catch (FileNotFoundException e2) {
                    reset();
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.dialog_error_list_title)
                            .setMessage(getString(R.string.dialog_error_list_message, "FilenotFoundException"))
                            .setPositiveButton(R.string.ok, null)
                            .show();
                    return;
                } catch (IOException e2) {
                    reset();
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.dialog_error_list_title)
                            .setMessage(getString(R.string.dialog_error_list_message, "IOException"))
                            .setPositiveButton(R.string.ok, null)
                            .show();
                    return;
                }
                // Jsonを読み取り、リスト化してダイアログを表示
                Gson gson = new Gson();
                DeliveryList list = gson.fromJson(listString, DeliveryList.class);
                ChoseListDialog dialog = new ChoseListDialog(MainActivity.this, list.app_list);
                dialog.setCompleteListner(chosedialogCompleteListner);
                dialog.setCancelListener(new ChoseListDialog.OnCancelListner() {
                    @Override
                    public void onCancel() {
                        reset();
                    }
                });
                dialog.show();
            } else {
                reset();
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.dialog_download_list)
                        .setMessage(getString(R.string.dialog_download_list_exception) + "\n" + e.toString())
                        .setPositiveButton(R.string.ok, null)
                        .show();
            }
        }
    };

    // Step2 アプリのダウンロード
    private final ChoseListDialog.OnCompleteListner chosedialogCompleteListner = new ChoseListDialog.OnCompleteListner() {
        @Override
        public void onComplete(AppPackage[] packagesList) {
            // 色々初期化したら始まり。
            downloadPackages = packagesList;
            setProgressMax(downloadPackages.length * 100);
            downloadedFiles = new ArrayList<>();
            appsCount = -1;
            downloadNextApp();
        }
    };

    private void downloadNextApp() {
        // カウントアップ
        appsCount++;
        if (appsCount < downloadPackages.length) { // まだまだダウンロードするものがある場合
            // 表示の更新
            titleView.setText(getString(R.string.text_download_app, downloadPackages[appsCount].name, appsCount + 1, downloadPackages.length));

            tmpInt = appsCount * 100;
            downloadedFiles.add(new File(saveDirectory, String.valueOf(appsCount) + ".apk"));
            FileDownloadTask task = new FileDownloadTask(downloadPackages[appsCount].url, downloadedFiles.get(appsCount));
            task.setUpdateListner(new FileDownloadTask.OnProgressUpdateListner() {
                @Override
                public void onUpdate(int i) {
                    setBar(tmpInt + i);
                }
            });
            task.setSuccessListner(new FileDownloadTask.OnSuccessListner() {
                @Override
                public void onSuccess(Exception e) {
                    if (e == null) {
                        downloadNextApp();
                    } else {
                        new AlertDialog.Builder(MainActivity.this)
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
        }
    }

    private void showAppInstall(File file) {
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setData(Uri.fromFile(file));
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivityForResult(intent, REQUEST_APP_INSTALL);
        Toast.makeText(this, R.string.toast_install_app, Toast.LENGTH_SHORT).show();
    }

    private void setProgressMax(int max) {
        progressMax = max;
    }

    private void reset() {
        setBar(0);
        button.setEnabled(true);
        titleView.setText(R.string.activity_main_title);
    }

    private void setBar(int progress) {
        progress = progress * 100 / progressMax;
        if (progress > 100) progress = 100;
        this.progress.setProgress(progress);
        this.progressView.setText(new StringBuilder().append(progress).append("%"));
    }

    private byte[] readAll(FileInputStream stream) throws IOException {
        ByteArrayOutputStream arrayOutput = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while (true) {
            len = stream.read(buffer);
            if (len < 0) {
                break;
            }
            arrayOutput.write(buffer, 0, len);
        }
        return arrayOutput.toByteArray();
    }
}

package com.github.ctab2labo.tweaksetuptool;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
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

public class MainActivity extends Activity implements View.OnClickListener{
    private TextView titleView;
    private ProgressBar progress;
    private TextView progressView;
    private Button button;
    private File deliverylistFile;

    private int progressMax = 100;
    private DeliveryList list;

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
        public void onSuccess(boolean bool) {
            if (bool) {
                String listString;
                try {
                    // ファイルを読み込む
                    FileInputStream inputStream = new FileInputStream(deliverylistFile);
                    listString = new String(readAll(inputStream));
                } catch (FileNotFoundException e) {
                    button.setEnabled(true);
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.dialog_error_list_title)
                            .setMessage(getString(R.string.dialog_error_list_message, "FilenotFoundException"))
                            .setPositiveButton(R.string.ok, null)
                            .show();
                    return;
                } catch (IOException e) {
                    button.setEnabled(true);
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.dialog_error_list_title)
                            .setMessage(getString(R.string.dialog_error_list_message, "IOException"))
                            .setPositiveButton(R.string.ok, null)
                            .show();
                    return;
                }
                // Jsonを読み取り、リスト化してダイアログを表示
                Gson gson = new Gson();
                list = gson.fromJson(listString, DeliveryList.class);
                ChoseListDialog dialog = new ChoseListDialog(MainActivity.this, list);
                dialog.setCompleteListner(chosedialogCompleteListner);
                dialog.setCancelListener(new ChoseListDialog.OnCancelListner() {
                    @Override
                    public void onCancel() {
                        button.setEnabled(true);
                    }
                });
                dialog.show();
            } else {
                button.setEnabled(true);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.dialog_download_list)
                        .setMessage(R.string.dialog_download_list_exception)
                        .setPositiveButton(R.string.ok, null)
                        .show();
            }
        }
    };

    private final ChoseListDialog.OnCompleteListner chosedialogCompleteListner = new ChoseListDialog.OnCompleteListner() {
        @Override
        public void onComplete(AppPackage[] packagesList) {

        }
    };

    private void setProgressMax(int max) {
        progressMax = max;
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

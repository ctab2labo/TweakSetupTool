package com.github.ctab2labo.tweaksetuptool.app_downloader;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.github.ctab2labo.tweaksetuptool.R;
import com.github.ctab2labo.tweaksetuptool.Util;
import com.github.ctab2labo.tweaksetuptool.app_downloader.adapter.DeliveryListAdapter;
import com.github.ctab2labo.tweaksetuptool.app_downloader.json.AppInfo;
import com.github.ctab2labo.tweaksetuptool.app_downloader.json.DeliveryList;
import com.github.ctab2labo.tweaksetuptool.app_downloader.task.DialogFileDownloadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class AppDownloaderActivity extends Activity {
    // やり方は汚いけど、とりあえずこれで...
    // static変数でりすとをわたす。
    static ArrayList<AppInfo> appInfoList;

    private ListView mainList;
    private Button startButton;
    private DeliveryList deliveryList;
    private DeliveryListAdapter adapter;

    private DialogFileDownloadTask dialogTask;
    private DialogFileDownloadTask.OnFinishedListener finishedListener = new DialogFileDownloadTask.OnFinishedListener() {
        @Override
        public void onSuccessful(Uri downloadUri, File downloadedFile) {
            Log.d("AppDownloader", downloadedFile.getPath());
            try {
                deliveryList = DeliveryList.importFromFile(downloadedFile);
            } catch (FileNotFoundException e) {/* 無視 */}
            dialogTask.remove();

            adapter = new DeliveryListAdapter(AppDownloaderActivity.this, deliveryList);
            adapter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    startButton.setEnabled(adapter.isAnyChecked()); // 何もチェックをつけていなかったらボタンを無効化する。
                }
            });
            mainList.setAdapter(adapter);
        }

        @Override
        public void onFailed(Uri downloadUri) {
            // 失敗したら、ダイアログを表示
            new AlertDialog.Builder(AppDownloaderActivity.this)
                    .setTitle(R.string.dialog_error_list_title)
                    .setMessage(R.string.dialog_error_list_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            AppDownloaderActivity.this.finish();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            AppDownloaderActivity.this.finish();
                        }
                    })
                    .show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_downloader);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mainList = (ListView) findViewById(R.id.main_list);
        startButton = (Button) findViewById(R.id.start_button);
        startButton.setEnabled(false);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AppDownloaderActivity.this, AppInstallActivity.class);
                appInfoList = adapter.getCheckedAppList();
                startActivity(intent);
            }
        });

        File deliveryFile = Util.createTempFile(this, "json");
        dialogTask = new DialogFileDownloadTask(this,
                Uri.parse(getString(R.string.url_list)),
                deliveryFile,
                getString(R.string.dialog_download_list_title),
                getString(R.string.dialog_download_list_message));
        dialogTask.setOnFinishedListener(finishedListener);
        dialogTask.start();
    }

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
}

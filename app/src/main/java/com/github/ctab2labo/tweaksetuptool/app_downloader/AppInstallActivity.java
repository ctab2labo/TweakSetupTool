package com.github.ctab2labo.tweaksetuptool.app_downloader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.github.ctab2labo.tweaksetuptool.R;
import com.github.ctab2labo.tweaksetuptool.app_downloader.adapter.DownloadListAdapter;
import com.github.ctab2labo.tweaksetuptool.app_downloader.json.AppInfo;
import com.github.ctab2labo.tweaksetuptool.app_downloader.json.AppInfoListParcelable;

import java.util.ArrayList;

public class AppInstallActivity extends Activity {
    public static final String EXTRA_DOWNLOAD_LIST = "extra_download_list"; // Parcelable(ArrayList<AppInfo>)

    private ListView listView;
    private Button cancelButton;
    private DownloadListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_install);
        listView = (ListView) findViewById(R.id.main_list);
        cancelButton = (Button) findViewById(R.id.cancel_button);

        AppInfoListParcelable parcelable;
        Intent intent = getIntent();
        if (intent == null) throw new RuntimeException("getIntent() returned null.");
        parcelable = intent.getParcelableExtra(EXTRA_DOWNLOAD_LIST);
        if (parcelable == null) throw new RuntimeException("appInfoList is null.");
        ArrayList<AppInfo> appInfoList = parcelable.getAppInfoList();

        adapter = new DownloadListAdapter(this, appInfoList);
        listView.setAdapter(adapter);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.cancelAllDownload();
                finish();
            }
        });
        adapter.startNextDownload();
    }
}

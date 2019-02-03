package com.github.ctab2labo.tweaksetuptool.app_downloader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.ctab2labo.tweaksetuptool.R;

import java.util.ArrayList;

public class DownloadListAdapter extends BaseAdapter {
    private final Context context;
    private final LayoutInflater inflater;
    private ArrayList<AppPackagePlus> appPackageList;

    public DownloadListAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void setAppPackageList(ArrayList<AppPackagePlus> appPackageList) {
        this.appPackageList = appPackageList;
    }

    @Override
    public int getCount() {
        return appPackageList.size();
    }

    @Override
    public Object getItem(int i) {
        return appPackageList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return appPackageList.get(i).getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.list_view_download_apk, viewGroup, false);
        ((TextView) view.findViewById(R.id.list_view_download_apk_title)).setText(appPackageList.get(i).getTitle());
        ((TextView) view.findViewById(R.id.list_view_download_apk_url)).setText(appPackageList.get(i).getUrl());
        ((TextView) view.findViewById(R.id.list_view_download_apk_percent)).setText(String.valueOf(appPackageList.get(i).getPercent()) + "%");
        ((ProgressBar) view.findViewById(R.id.list_view_download_apk_bar)).setProgress(appPackageList.get(i).getPercent());

        return view;
    }
}

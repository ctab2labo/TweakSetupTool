package com.github.ctab2labo.tweaksetuptool.app_downloader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.ctab2labo.tweaksetuptool.R;

import java.util.ArrayList;

public class InstallListAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private ArrayList<DownloadedFile> downloadedFileList = new ArrayList<>();

    public InstallListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void setDownloadedFileList(ArrayList<DownloadedFile> downloadedFileList) {
        this.downloadedFileList = downloadedFileList;
    }

    @Override
    public int getCount() {
        return downloadedFileList.size();
    }

    @Override
    public Object getItem(int position) {
        return downloadedFileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return downloadedFileList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // ビューをセット
        convertView = inflater.inflate(R.layout.list_view_install_apk, parent, false);
        ((TextView) convertView.findViewById(R.id.list_view_install_apk_title)).setText(downloadedFileList.get(position).getTitle());
        ((TextView) convertView.findViewById(R.id.list_view_install_apk_path)).setText(downloadedFileList.get(position).getPath());
        convertView.findViewById(R.id.list_view_install_apk_progress).setVisibility(downloadedFileList.get(position).isEnabledProgress() ? View.VISIBLE : View.GONE);
        return convertView;
    }
}

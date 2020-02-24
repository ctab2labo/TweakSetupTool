package com.github.ctab2labo.tweaksetuptool.app_downloader.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.ctab2labo.tweaksetuptool.R;
import com.github.ctab2labo.tweaksetuptool.app_downloader.json.AppInfo;
import com.github.ctab2labo.tweaksetuptool.app_downloader.task.FileDownloadTask;
import com.github.ctab2labo.tweaksetuptool.app_downloader.task.InstallBaseTask;

import java.io.File;
import java.util.ArrayList;

public class DownloadListAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<AppInfo> downloadList;
    // 値の変更を通知するだけでいい。
    private FileDownloadTask.OnFinishedListener onDownloadFinishedListener = new FileDownloadTask.OnFinishedListener() {
        @Override
        public void onSuccessful(Uri downloadUri, File downloadedFile) {
            DownloadListAdapter.this.notifyDataSetChanged();
        }

        @Override
        public void onFailed(Uri downloadUri) {
            DownloadListAdapter.this.notifyDataSetChanged();
        }
    };
    private FileDownloadTask.OnProgressUpdateListener onProgressUpdateListener = new FileDownloadTask.OnProgressUpdateListener() {
        @Override
        public void onProgressUpdate(Uri downloadUri, int progress) {
            DownloadListAdapter.this.notifyDataSetChanged();
        }
    };
    private InstallBaseTask.OnFinishListener onInstallFinishListener = new InstallBaseTask.OnFinishListener() {
        @Override
        public void onSuccessful(File filePath) {
            DownloadListAdapter.this.notifyDataSetChanged();
        }

        @Override
        public void onFailed(File filePath) {
            DownloadListAdapter.this.notifyDataSetChanged();
        }
    };

    public DownloadListAdapter(Context context, ArrayList<AppInfo> downloadList) {
        this.context = context;
        this.downloadList = downloadList;
    }

    @Override
    public int getCount() {
        return downloadList.size();
    }

    @Override
    public Object getItem(int i) {
        return downloadList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.list_view_download, viewGroup, false);
            holder = new ViewHolder();
            holder.title = (TextView) view.findViewById(R.id.title);
            holder.description = (TextView) view.findViewById(R.id.description);
            holder.textProgress = (TextView) view.findViewById(R.id.text_progress);
            holder.barProgress = (ProgressBar) view.findViewById(R.id.bar_progress);
            holder.barProgress.setMax(100);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        AppInfo appInfo = downloadList.get(i);
        holder.title.setText(appInfo.getName());
        holder.description.setText(appInfo.getSummary());

        int progress = appInfo.getDownloadProgress();
        switch (progress) {
            case AppInfo.PROGRESS_NONE:
                holder.description.setText(context.getString(R.string.progress_none));
                holder.textProgress.setText("");
                holder.barProgress.setIndeterminate(true);
                break;
            case AppInfo.PROGRESS_DOWNLOAD_FAILED:
                holder.description.setText(context.getString(R.string.progress_failed_download));
                holder.textProgress.setText("");
                holder.barProgress.setVisibility(View.INVISIBLE);
                break;
            case AppInfo.PROGRESS_INSTALL_FAILED:
                holder.description.setText(context.getString(R.string.progress_failed_install));
                holder.textProgress.setText("");
                holder.barProgress.setVisibility(View.INVISIBLE);
                break;
            case AppInfo.PROGRESS_INSTALL_SUCCESSFUL:
                holder.description.setText(context.getString(R.string.progress_install_successful));
                holder.textProgress.setText("");
                holder.barProgress.setVisibility(View.INVISIBLE);
                break;
            case AppInfo.PROGRESS_INSTALLING:
                holder.description.setText(context.getString(R.string.progress_installing));
                holder.textProgress.setText("");
                holder.barProgress.setIndeterminate(true);
                break;
            default: // ほかの値はすべてダウンロード中の値
                holder.description.setText(context.getString(R.string.progress_downloading));
                holder.textProgress.setText(context.getString(R.string.progress, progress));
                holder.barProgress.setIndeterminate(false);
                holder.barProgress.setProgress(progress);
        }
        return view;
    }

    public void startAllDownload() {
        int index = 0;

        for (AppInfo appInfo : downloadList) {
            appInfo.startDownload(context);
            appInfo.setOnDownloadFinishedListener(onDownloadFinishedListener);
            appInfo.setOnProgressUpdateListener(onProgressUpdateListener);
            appInfo.setOnInstallFinishListener(onInstallFinishListener);
            downloadList.set(index, appInfo);
            index++;
        }
    }

    public void cancelAllDownload() {
        int index = 0;
        for (AppInfo appInfo : downloadList) {
            appInfo.cancelDownload();
            downloadList.set(index, appInfo);
            index++;
        }
    }

    private static class ViewHolder {
        private TextView title;
        private TextView description;
        private TextView textProgress;
        private ProgressBar barProgress;
    }
}

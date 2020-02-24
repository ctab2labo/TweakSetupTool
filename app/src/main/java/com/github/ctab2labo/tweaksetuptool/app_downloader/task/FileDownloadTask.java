package com.github.ctab2labo.tweaksetuptool.app_downloader.task;

import android.app.DownloadManager;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

import java.io.File;

public class FileDownloadTask {
    public static final int PROGRESS_MAX = 100;
    private final Context context;
    private final Uri downloadUri;
    private final File downloadedFile;
    private final CharSequence title;
    private final CharSequence description;
    private final DownloadManager downloadManager;

    private OnProgressUpdateListener onProgressUpdateListener;
    private OnFinishedListener onFinishedListener;
    private long id;
    private Uri contentId;
    public FileDownloadTask(Context context, Uri uri, File path) {
        this(context, uri, path, null, null);
    }

    public FileDownloadTask(Context context, Uri uri, File path, CharSequence title, CharSequence description) {
        this.context = context;
        this.downloadUri = uri;
        this.downloadedFile = path;
        this.title = title;
        this.description = description;
        this.downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (this.downloadManager == null) throw new RuntimeException("DownloadManager returned null");
    }

    /**
     * ダウンロードを開始します。
     */
    public void start() {
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setDestinationUri(Uri.fromFile(downloadedFile));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        if (title != null) {
            request.setTitle(title);
        }
        if (description != null) {
            request.setDescription(description);
        }

        // idを取得してダウンロード開始
        long id = downloadManager.enqueue(request);
        setDownloadId(id);
    }

    public void cancel() {
        downloadManager.remove(id);
    }

    public void remove() {
        downloadManager.remove(id);
    }

    /**
     * ダウンロードの変化が通知されます。
     */
    protected void onEvent() {
        queryStatus();
    }

    /**
     * ダウンロードの状態を確認します。
     */
    private void queryStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);

        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int statusIndex  = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = cursor.getInt(statusIndex);
            switch (status) {
                case DownloadManager.STATUS_RUNNING: // 進行中ならどれくらい進んでいるかを通知
                    int sizeIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                    int downloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                    long size = cursor.getInt(sizeIndex);
                    long downloaded = cursor.getInt(downloadedIndex);
                    int progress = 0;
                    if (size != -1) progress = (int) (downloaded * PROGRESS_MAX / size);
                    onProgressUpdate(progress);
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    onSuccessful();
                    break;
                case DownloadManager.STATUS_FAILED:
                    onFailed();
                    break;
            }
        }
    }

    private void setDownloadId(long id) {
        this.id = id;
        this.contentId = Uri.parse("content://downloads/my_downloads/" + id);

        // ついでにオブザーバーも設定
        context.getContentResolver().registerContentObserver(Uri.parse("content://downloads/my_downloads"), true, new DownloadObserver(this));
    }

    /**
     * リスナーに進行状況を通知
     */
    protected void onProgressUpdate(int progress) {
        if (onProgressUpdateListener != null) {
            onProgressUpdateListener.onProgressUpdate(downloadUri, progress);
        }
    }

    /**
     * リスナーに失敗したことを通知
     */
    protected void onFailed() {
        if (onFinishedListener != null) {
            onFinishedListener.onFailed(downloadUri);
        }
    }

    /**
     * リスナーに成功したことを通知
     */
    protected void onSuccessful() {
        if (onFinishedListener != null) {
            onFinishedListener.onSuccessful(downloadUri, downloadedFile);
        }
    }

    public void setOnFinishedListener(OnFinishedListener onFinishedListener) {
        this.onFinishedListener = onFinishedListener;
    }

    public void setOnProgressUpdateListener(OnProgressUpdateListener onProgressUpdateListener) {
        this.onProgressUpdateListener = onProgressUpdateListener;
    }

    private static class DownloadObserver extends ContentObserver {
        private final FileDownloadTask task;

        private DownloadObserver(FileDownloadTask task) {
            super(new Handler());
            this.task = task;
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange);
            task.onEvent();
        }
    }

    public interface OnProgressUpdateListener {
        void onProgressUpdate(Uri downloadUri, int progress);
    }

    public interface OnFinishedListener {
        void onSuccessful(Uri downloadUri, File downloadedFile);

        void onFailed(Uri downloadUri);
    }
}

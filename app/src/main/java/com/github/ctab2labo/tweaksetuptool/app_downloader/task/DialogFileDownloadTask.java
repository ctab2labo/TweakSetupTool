package com.github.ctab2labo.tweaksetuptool.app_downloader.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;

import java.io.File;

public class DialogFileDownloadTask extends FileDownloadTask {
    private final Context context;
    private final String title;
    private final String description;

    private ProgressDialog dialog;

    public DialogFileDownloadTask(Context context, Uri uri, File path, String title, String description) {
        super(context, uri, path, title, description);
        this.context = context;
        this.title = title;
        this.description = description;

        dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMax(DialogFileDownloadTask.PROGRESS_MAX);
        dialog.setProgress(0);
        dialog.setTitle(title);
        dialog.setMessage(description);
        dialog.setCancelable(false);
        dialog.setIndeterminate(false);
    }

    @Override
    public void start() {
        super.start();
        dialog.show();
    }

    @Override
    protected void onSuccessful() {
        super.onSuccessful();
        dialog.dismiss();
    }

    @Override
    protected void onFailed() {
        super.onFailed();
        dialog.dismiss();
    }

    @Override
    protected void onProgressUpdate(int progress) {
        super.onProgressUpdate(progress);
        dialog.setProgress(progress);
    }
}

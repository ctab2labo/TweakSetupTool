package com.github.ctab2labo.tweaksetuptool.app_downloader.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.ctab2labo.tweaksetuptool.R;
import com.github.ctab2labo.tweaksetuptool.app_downloader.task.FileDownloadTask;

import java.io.File;

// ファイルをダウンロードするためのダイアログ
public class FileDownloadProgressDialog extends AlertDialog {
    private TextView message;
    private ProgressBar bar;
    private TextView percent;

    private int progressMax = 100;
    private FileDownloadTask downloadTask;

    private OnCompletedListener onCompletedListener;

    private final FileDownloadTask.OnCompletedListener onSuccessListener = new FileDownloadTask.OnCompletedListener() {
        @Override
        public void onCompleted(Exception e) {
            complete(e);
        }
    };

    private FileDownloadProgressDialog(Context context, String downloadUrl, File writeFile) {
        super(context);
        View view = View.inflate(context,R.layout.dialog_downloader_progress_list_download,null);

        message = (TextView) view.findViewById(R.id.dialog_progress_message);
        bar = (ProgressBar) view.findViewById(R.id.dialog_progress_bar);
        percent = (TextView) view.findViewById(R.id.dialog_progress_percent);

        setView(view);

        // ダウンロードタスクを初期化
        downloadTask = new FileDownloadTask(downloadUrl, writeFile);
        downloadTask.setUpdateListener(new FileDownloadTask.OnProgressUpdateListener() {
            @Override
            public void onUpdate(int i) {
                setProgress(i);
            }
        });
        downloadTask.setOnCompletedListener(onSuccessListener);
    }

    @Override
    public void show() {
        super.show();
        downloadTask.execute();
    }

    @Override
    public void setMessage(CharSequence message) {
        this.message.setText(message);
    }

    private void complete(Exception e) {
        if (onCompletedListener != null) {
            onCompletedListener.onCompleted(e);
        }
        this.dismiss();
    }

    private void setProgress(int i) {
        i = i * 100 / progressMax;
        if (i > 100) i = 100;
        bar.setProgress(i);
        percent.setText(String.valueOf(i) + "%");
    }

    public void setOnCompletedListener(OnCompletedListener onCompletedListener) {
        this.onCompletedListener = onCompletedListener;
    }

    public interface OnCompletedListener {
        void onCompleted(Exception e);
    }

    public static class Builder {
        private final Context context;

        private final FileDownloadProgressDialog dialog;

        public Builder(Context context, String downloadUrl, File writeFile) {
            this.context = context;
            dialog = new FileDownloadProgressDialog(context, downloadUrl, writeFile);
        }

        public Builder setOnCompletedListener(OnCompletedListener listener) {
            dialog.setOnCompletedListener(listener);
            return this;
        }
        public Builder setCancelable(boolean cancelable) {
            dialog.setCancelable(cancelable);
            return this;
        }

        public Builder setPositiveButton(CharSequence text, DialogInterface.OnClickListener listener) {
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, text, listener);
            return this;
        }

        public Builder setNegativeButton(CharSequence text, DialogInterface.OnClickListener listener) {
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, text, listener);
            return this;
        }

        public Builder setOnCancelListener(DialogInterface.OnCancelListener listener) {
            dialog.setOnCancelListener(listener);
            return this;
        }

        public Builder setTitle(int rId) {
            dialog.setTitle(context.getString(rId));
            return this;
        }

        public Builder setTitle(CharSequence text) {
            dialog.setTitle(text);
            return this;
        }

        public Builder setMessage(int rId) {
            dialog.setMessage(context.getString(rId));
            return this;
        }

        public Builder setMessage(CharSequence text) {
            dialog.setMessage(text);
            return this;
        }

        public FileDownloadProgressDialog show() {
            dialog.show();
            return dialog;
        }
    }
}

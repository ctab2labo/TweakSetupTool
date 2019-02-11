package com.github.ctab2labo.tweaksetuptool.app_downloader.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    private CharSequence cancelText = "";
    private boolean isCancellable = true;

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

        // ボタンに必要なものを初期化
        cancelText = context.getText(R.string.cancel);
        this.setButton(DialogInterface.BUTTON_NEGATIVE, cancelText, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FileDownloadProgressDialog.this.cancel();
            }
        });
        this.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancelDownload();
            }
        });
    }

    @Override
    public void show() {
        super.show();
        downloadTask.execute();
        Button negativeButton = getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setVisibility(isCancellable ? View.VISIBLE : View.GONE);
        negativeButton.setHeight(isCancellable ? ViewGroup.LayoutParams.WRAP_CONTENT : 0);
        negativeButton.setText(cancelText);
    }

    @Override
    public void setCancelable(boolean flag) {
        super.setCancelable(flag);
        isCancellable = flag;
        Button negativeButton = getButton(DialogInterface.BUTTON_NEGATIVE);
        if (negativeButton != null) {
            negativeButton.setVisibility(isCancellable ? View.VISIBLE : View.GONE);
        }
    }

    public void setCancelButtonText(CharSequence text) {
        cancelText = text;
        Button negativeButton = getButton(DialogInterface.BUTTON_NEGATIVE);
        if (negativeButton != null) {
            getButton(DialogInterface.BUTTON_NEGATIVE).setText(text);
        }
    }

    @Override
    public void setMessage(CharSequence message) {
        this.message.setText(message);
        this.message.setVisibility(message.equals("") ? View.GONE : View.VISIBLE);
    }

    private void complete(Exception e) {
        if (onCompletedListener != null) {
            onCompletedListener.onCompleted(e);
        }
        this.dismiss();
    }

    @Override
    public void cancel() {
        cancelDownload();
        super.cancel();
    }

    private void cancelDownload() {
        downloadTask.cancel(false);
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
        public Builder setCancelButtonText(CharSequence text) {
            dialog.setCancelButtonText(text);
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

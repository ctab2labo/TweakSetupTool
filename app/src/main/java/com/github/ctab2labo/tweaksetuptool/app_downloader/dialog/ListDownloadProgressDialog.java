package com.github.ctab2labo.tweaksetuptool.app_downloader.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.ctab2labo.tweaksetuptool.R;
import com.github.ctab2labo.tweaksetuptool.app_downloader.json.DeliveryList;
import com.github.ctab2labo.tweaksetuptool.app_downloader.task.FileDownloadTask;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

// リストをダウンロードするためのダイアログ
public class ListDownloadProgressDialog extends AlertDialog {
    private TextView message;
    private ProgressBar bar;
    private TextView percent;

    private int progressMax = 100;
    private FileDownloadTask downloadTask;
    private final File deliveryListFile;

    private OnCompletedListener onCompletedListener;

    private final FileDownloadTask.OnCompletedListener onSuccessListner = new FileDownloadTask.OnCompletedListener() {
        @Override
        public void onCompleted(Exception e) {
            if (e == null) {
                try {
                    // ファイルを読み込む
                    FileInputStream inputStream = new FileInputStream(deliveryListFile);
                    String listString = new String(readAll(inputStream));
                    Gson gson = new Gson();
                    DeliveryList deliveryList = gson.fromJson(listString, DeliveryList.class);
                    complete(deliveryList, null);
                } catch (Exception e2) {
                    complete(null, e2);
                }
            } else {
                complete(null, e);
            }
        }
    };

    protected ListDownloadProgressDialog(Context context) {
        super(context);
        View view = View.inflate(context,R.layout.dialog_downloader_progress_list_download,null);

        message = (TextView) view.findViewById(R.id.dialog_progress_message);
        bar = (ProgressBar) view.findViewById(R.id.dialog_progress_bar);
        percent = (TextView) view.findViewById(R.id.dialog_progress_percent);

        setView(view);

        deliveryListFile = new File(context.getFilesDir(),"delivery_list.json");

        // ダウンロードタスクを初期化
        downloadTask = new FileDownloadTask(getContext().getString(R.string.url_list), deliveryListFile);
        downloadTask.setUpdateListener(new FileDownloadTask.OnProgressUpdateListener() {
            @Override
            public void onUpdate(int i) {
                setProgress(i);
            }
        });
        downloadTask.setOnCompletedListener(onSuccessListner);
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

    private void complete(DeliveryList deliveryList, Exception e) {
        if (onCompletedListener != null) {
            onCompletedListener.onCompleted(deliveryList, e);
        }
        this.dismiss();
    }

    private void setProgressMax(int i) {
        progressMax = i;
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
        void onCompleted(DeliveryList deliveryList, Exception e);
    }

    public static class Builder {
        private final Context context;

        private final ListDownloadProgressDialog dialog;

        public Builder(Context context) {
            this.context = context;
            dialog = new ListDownloadProgressDialog(context);
        }

        public Builder setOnCompletedListener(OnCompletedListener listener) {
            dialog.setOnCompletedListener(listener);
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

        public ListDownloadProgressDialog show() {
            dialog.show();
            return dialog;
        }
    }
}

package com.github.ctab2labo.tweaksetuptool.app_downloader.task;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import static com.github.ctab2labo.tweaksetuptool.Common.TAG;

public class FileDownloadTask extends AsyncTask<Void, Integer, Exception> {
    private final int BUFFER_SIZE = 1024;
    private String urlString;
    private File file;
    private OnProgressUpdateListener updateListener;
    private OnCompletedListener successListener;
    private long totalByte;
    private int previosInt;
    private byte[] buffer = new byte[BUFFER_SIZE];

    public FileDownloadTask(String url, File file) {
        urlString = url;
        this.file = file;
    }

    @Override
    protected Exception doInBackground(Void... obj) {
        // URLをもとに初期化
        URLConnection connection;
        FileOutputStream fileOutputStream;
        InputStream inputStream;
        try {
            URL url = new URL(urlString);
            connection = url.openConnection();
            connection.addRequestProperty("Connection", "close");
            inputStream = connection.getInputStream();
            fileOutputStream = new FileOutputStream(file);
        } catch (Exception e) {
            Log.e(TAG, "FileDownloadTask:Exception", e);
            return e;
        }
        connection.setReadTimeout(5000);
        connection.setConnectTimeout(30000);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, BUFFER_SIZE);
        totalByte = connection.getContentLength();
        long currentByte = 0;
        previosInt = 0;

        // 読み取れたデータだけ書き込む。
        try {
            int len;
            while((len = bufferedInputStream.read(buffer)) >= 0){
                fileOutputStream.write(buffer, 0, len);
                currentByte += len;
                updateProgress(currentByte, totalByte);
                if (isCancelled()) {
                    // もとに戻して終了
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    this.file.delete();
                    bufferedInputStream.close();
                    return null;
                }
            }
        } catch(IOException e) {
            Log.e(TAG, "FileDownloadTask:IOException", e);
            return e;
        }

        try {
            fileOutputStream.flush();
            fileOutputStream.close();
            bufferedInputStream.close();
        } catch(IOException e) {
            Log.e(TAG, "FileDownloadTask:IOException", e);
            return e;
        }
        return null;
    }

    private void updateProgress(long i, long i2) {
        // 100分率を計算。変更があれば、パブリッシュ
        i = i * 100 / i2;
        i = i > 100 ? 100 : i;
        if (previosInt != i) {
            previosInt = (int) i;
            publishProgress(previosInt);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (updateListener != null) {
            updateListener.onUpdate(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Exception aException) {
        if (successListener != null && (! isCancelled())) {
            successListener.onCompleted(aException);
        }
    }

    public interface OnProgressUpdateListener {
        void onUpdate(int i);
    }

    public interface OnCompletedListener {
        void onCompleted(Exception e);
    }

    public void setOnCompletedListener(OnCompletedListener successListener) {
        this.successListener = successListener;
    }

    public void setUpdateListener(OnProgressUpdateListener updateListener) {
        this.updateListener = updateListener;
    }
}

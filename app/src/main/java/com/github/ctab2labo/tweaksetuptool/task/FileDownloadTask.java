package com.github.ctab2labo.tweaksetuptool.task;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class FileDownloadTask extends AsyncTask<Void, Integer, Boolean> {
    private final String TAG = "TweakSetupTool";
    private final int BUFFER_SIZE = 1024;
    private String urlString;
    private File file;
    private OnProgressUpdateListner updateListner;
    private OnSuccessListner successListner;
    private int totalByte;
    private int previosInt;
    private byte[] buffer = new byte[BUFFER_SIZE];

    public FileDownloadTask(String url, File file) {
        urlString = url;
        this.file = file;
    }

    @Override
    protected Boolean doInBackground(Void... obj) {
        // URLをもとに初期化
        URLConnection connection;
        FileOutputStream fileOutputStream;
        InputStream inputStream;
        try {
            URL url = new URL(urlString);
            connection = url.openConnection();
            inputStream = connection.getInputStream();
            fileOutputStream = new FileOutputStream(file);
        } catch (Exception e) {
            Log.e(TAG, "FileDownloadTask:Exception", e);
            return false;
        }
        connection.setReadTimeout(5000);
        connection.setConnectTimeout(30000);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, BUFFER_SIZE);
        totalByte = connection.getContentLength();
        int currentByte = 0;
        previosInt = 0;

        // 読み取れたデータだけ書き込む。
        try {
            int len;
            while((len = bufferedInputStream.read(buffer)) != -1){
                fileOutputStream.write(buffer, 0, len);
                currentByte += len;
                updateProgress(currentByte, totalByte);
            }
        } catch(IOException e) {
            Log.d(TAG, "FileDownloadTask:IOException", e);
            return false;
        }

        try {
            fileOutputStream.flush();
            fileOutputStream.close();
            bufferedInputStream.close();
        } catch(IOException e) {
            Log.e(TAG, "FileDownloadTask:IOException", e);
            return false;
        }
        return true;
    }

    private void updateProgress(int i, int i2) {
        // 100分率を計算。変更があれば、パブリッシュ
        i = i * 100 / i2;
        i = i > 100 ? 100 : i;
        if (previosInt != i) {
            previosInt = i;
            publishProgress(i);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (updateListner != null) {
            updateListner.onUpdate(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (successListner != null) {
            successListner.onSuccess(aBoolean);
        }
    }

    public interface OnProgressUpdateListner {
        void onUpdate(int i);
    }

    public interface OnSuccessListner {
        void onSuccess(boolean bool);
    }

    public void setSuccessListner(OnSuccessListner successListner) {
        this.successListner = successListner;
    }

    public void setUpdateListner(OnProgressUpdateListner updateListner) {
        this.updateListner = updateListner;
    }
}

package com.github.ctab2labo.tweaksetuptool.app_downloader.json;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.github.ctab2labo.tweaksetuptool.R;
import com.github.ctab2labo.tweaksetuptool.Util;
import com.github.ctab2labo.tweaksetuptool.app_downloader.task.FileDownloadTask;
import com.github.ctab2labo.tweaksetuptool.app_downloader.task.InstallBaseTask;

import java.io.File;

public class AppInfo implements Parcelable {
    public static final int PROGRESS_NONE = -1;
    public static final int PROGRESS_INSTALLING = -2;
    public static final int PROGRESS_DOWNLOAD_FAILED = -3;
    public static final int PROGRESS_INSTALL_FAILED = -4;
    public static final int PROGRESS_INSTALL_SUCCESSFUL = -5;

    // JSONから取得
    public String name;
    public String url;
    public String summary;

    // いろいろと使用
    private Context context;
    private boolean isChecked;
    private int progress;
    private FileDownloadTask task;
    private FileDownloadTask.OnFinishedListener onDownloadFinishedListener = new FileDownloadTask.OnFinishedListener() {
        @Override
        public void onSuccessful(Uri downloadUri, File downloadedFile) {
            progress = PROGRESS_INSTALLING;
            installFile(downloadedFile);

            if (onDownloadFinishedListener2 != null) {
                onDownloadFinishedListener2.onSuccessful(downloadUri, downloadedFile);
            }
        }

        @Override
        public void onFailed(Uri downloadUri) {
            progress = PROGRESS_DOWNLOAD_FAILED;
            if (onDownloadFinishedListener2 != null) {
                onDownloadFinishedListener2.onFailed(downloadUri);
            }
        }
    };
    private FileDownloadTask.OnFinishedListener onDownloadFinishedListener2;
    private FileDownloadTask.OnProgressUpdateListener onProgressUpdateListener = new FileDownloadTask.OnProgressUpdateListener() {
        @Override
        public void onProgressUpdate(Uri downloadUri, int progress) {
            AppInfo.this.progress = progress;
            if (onProgressUpdateListener2 != null) {
                onProgressUpdateListener2.onProgressUpdate(downloadUri, progress);
            }
        }
    };
    private InstallBaseTask.OnFinishListener onInstallFinishListener = new InstallBaseTask.OnFinishListener() {
        @Override
        public void onSuccessful(File filePath) {
            progress = PROGRESS_INSTALL_SUCCESSFUL;

            if (onInstallFinishListener2 != null) {
                onInstallFinishListener2.onSuccessful(filePath);
            }
        }

        @Override
        public void onFailed(File filePath) {
            progress = PROGRESS_INSTALL_FAILED;

            if (onInstallFinishListener2 != null) {
                onInstallFinishListener2.onFailed(filePath);
            }
        }
    };
    private InstallBaseTask.OnFinishListener onInstallFinishListener2;
    private FileDownloadTask.OnProgressUpdateListener onProgressUpdateListener2;

    public AppInfo() {
        progress = -1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Uri getUrl() {
        return Uri.parse(url);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    /**
     * ダウンロードタスクを実行します。
     */
    public int getDownloadProgress() {
        return progress;
    }

    /**
     * 自身にある情報をもとにダウンロードタスクを実行します。
     */
    public void startDownload(Context context) {
        if (task == null) { // ただし、既に実行されていたら無効
            this.context = context;
            FileDownloadTask task = new FileDownloadTask(
                    context,
                    getUrl(),
                    Util.createTempFile(context, "apk"),
                    context.getString(R.string.notify_downloading_title),
                    name
            );
            task.setOnFinishedListener(onDownloadFinishedListener);
            task.setOnProgressUpdateListener(onProgressUpdateListener);
            task.start();
        }
    }

    private void installFile(File apk) {
        InstallBaseTask task = InstallBaseTask.getInstaller(context, apk);
        task.setOnFinishListener(onInstallFinishListener);
        task.install();
    }

    /**
     * ダウンロードタスクをキャンセルします。
     */
    public void cancelDownload() {
        if (task != null) {
            task.cancel();
            task = null;
            context = null;
        }
    }

    /**
     * ダウンロードタスクのプログレスリスナー
     */
    public void setOnProgressUpdateListener(FileDownloadTask.OnProgressUpdateListener onProgressUpdateListener) {
        this.onProgressUpdateListener2 = onProgressUpdateListener;
        if (task != null) {
            task.setOnProgressUpdateListener(onProgressUpdateListener);
        }
    }

    /**
     * ダウンロードタスクの終了リスナー
     */
    public void setOnDownloadFinishedListener(FileDownloadTask.OnFinishedListener onDownloadFinishedListener) {
        this.onDownloadFinishedListener2 = onDownloadFinishedListener;
        if (task != null) {
            task.setOnFinishedListener(onDownloadFinishedListener);
        }
    }

    public void setOnInstallFinishListener(InstallBaseTask.OnFinishListener onInstallFinishListener) {
        this.onInstallFinishListener2 = onInstallFinishListener;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(url);
        parcel.writeString(summary);
        parcel.writeString(String.valueOf(isChecked));
        parcel.writeInt(progress);
    }


    private AppInfo(Parcel parcel) {
        this();
        name = parcel.readString();
        url = parcel.readString();
        summary = parcel.readString();
        isChecked = Boolean.parseBoolean(parcel.readString());
        progress = parcel.readInt();
    }

    public static final Parcelable.Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
        @Override
        public AppInfo createFromParcel(Parcel parcel) {
            return new AppInfo(parcel);
        }

        @Override
        public AppInfo[] newArray(int i) {
            return new AppInfo[i];
        }
    };
}

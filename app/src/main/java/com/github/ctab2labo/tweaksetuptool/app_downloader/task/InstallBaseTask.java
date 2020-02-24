package com.github.ctab2labo.tweaksetuptool.app_downloader.task;

import android.content.Context;

import java.io.File;

public abstract class InstallBaseTask {
    private OnFinishListener onFinishListener;
    private File installFile;

    public InstallBaseTask(File installFile) {
        this.installFile = installFile;
    }

    /**
     * インストールを実行します。
     */
    public abstract void install();

    /*
     * 継承するときに必ず1回は使ってください。
     */
    protected void successful() {
        if (onFinishListener != null) {
            onFinishListener.onSuccessful(installFile);
        }
    }

    /*
     * 継承するときに必ず1回は使ってください。
     */
    protected void failed() {
        if (onFinishListener != null) {
            onFinishListener.onFailed(installFile);
        }
    }

    public void setOnFinishListener(OnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }

    /**
     * 端末ごとに適したインストーラーを返します。
     */
    public static InstallBaseTask getInstaller(Context context, File installFile) {
        return new InstallDefaultTask(context, installFile);
    }

    public interface OnFinishListener {
        void onSuccessful(File filePath);

        void onFailed(File filePath);
    }
}

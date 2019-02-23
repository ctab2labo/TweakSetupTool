package com.github.ctab2labo.tweaksetuptool.app_downloader.adapter;

import com.github.ctab2labo.tweaksetuptool.app_downloader.json.AppPackage;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class DownloadedFile implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String title = "";
    private String path = "";
    private boolean isEnabledProgress = false;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isEnabledProgress() {
        return isEnabledProgress;
    }

    public void setEnabledProgress(boolean enabledProgress) {
        isEnabledProgress = enabledProgress;
    }

    /**
     * ファイルとアップパッケージプラスリストをDownloadedFileListに変換します。
     * @param appPackagePercentList アップパッケージプラスリスト
     * @param fileList ダウンロードしたファイル
     * @return 変換したDownloadedFileList
     */
    public static ArrayList<DownloadedFile> fileWithAppPackagePlusListToDownloadedFileList(ArrayList<AppPackagePercent> appPackagePercentList, ArrayList<File> fileList) {
        if (appPackagePercentList.size() != fileList.size()) return null; // サイズが一致しないならnullを返す。

        // 変換して返す
        ArrayList<DownloadedFile> downloadedFileList = new ArrayList<>();
        int listSize = appPackagePercentList.size();
        for (int i=0;i<listSize;i++) {
            DownloadedFile downloadedFile = new DownloadedFile();
            downloadedFile.setTitle(appPackagePercentList.get(i).getName());
            downloadedFile.setPath(fileList.get(i).getPath());
            downloadedFile.setEnabledProgress(false);
            downloadedFileList.add(downloadedFile);
        }
        return downloadedFileList;
    }

    /**
     * ファイルとアップパッケージリストをDownloadedFileListに変換します。
     * @param appPackageList アップパッケージリスト
     * @param fileList ダウンロードしたファイル
     * @return 変換したDownloadedFileList
     */
    public static ArrayList<DownloadedFile> fileWithAppPackageListToDownloadedFileList(ArrayList<AppPackage> appPackageList, ArrayList<File> fileList) {
        if (appPackageList.size() != fileList.size()) return null; // サイズが一致しないならnullを返す。

        // 変換して返す
        ArrayList<DownloadedFile> downloadedFileList = new ArrayList<>();
        int listSize = appPackageList.size();
        for (int i=0;i<listSize;i++) {
            DownloadedFile downloadedFile = new DownloadedFile();
            downloadedFile.setTitle(appPackageList.get(i).name);
            downloadedFile.setPath(fileList.get(i).getPath());
            downloadedFile.setEnabledProgress(true);
            downloadedFileList.add(downloadedFile);
        }
        return downloadedFileList;
    }
}

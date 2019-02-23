package com.github.ctab2labo.tweaksetuptool.app_downloader.adapter;

import com.github.ctab2labo.tweaksetuptool.app_downloader.json.AppPackage;

public class AppPackageCheck {
    private long id;
    private AppPackage appPackage;
    private boolean isChecked;

    public AppPackageCheck() {}

    public AppPackageCheck(AppPackage appPackage) {
        this.appPackage = appPackage;
        this.isChecked = true; // 最初からオンにしておく
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return appPackage.name;
    }

    public String getUrl() {
        return appPackage.url;
    }

    public String getSummary() { return appPackage.summary; }

    public AppPackage getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(AppPackage appPackage) {
        this.appPackage = appPackage;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}

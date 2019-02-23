package com.github.ctab2labo.tweaksetuptool.app_downloader.adapter;

import com.github.ctab2labo.tweaksetuptool.app_downloader.json.AppPackage;

public class AppPackagePercent {
    private long id;
    private AppPackage appPackage;
    private int percent;

    public AppPackagePercent() {}

    public AppPackagePercent(AppPackage appPackage) {
        this.appPackage = appPackage;
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

    public AppPackage getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(AppPackage appPackage) {
        this.appPackage = appPackage;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }
}

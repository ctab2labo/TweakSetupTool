package com.github.ctab2labo.tweaksetuptool.app_downloader.adapter;

import com.github.ctab2labo.tweaksetuptool.app_downloader.json.AppPackage;

public class AppPackagePlus {
    private long id;
    private String title;
    private String url;
    private int percent;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public static AppPackagePlus toPlus(AppPackage appPackage) {
        AppPackagePlus plus = new AppPackagePlus();
        plus.setTitle(appPackage.name);
        plus.setUrl(appPackage.url);
        return plus;
    }
}

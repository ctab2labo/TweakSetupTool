package com.github.ctab2labo.tweaksetuptool.self_update.json;

import com.google.gson.Gson;

public class AppInfo {
    private AppInfo() {}

    public int app_version;
    public String app_dlurl;
    public String summary;

    public static AppInfo fromJson(String jsonString) {
        Gson gson = new Gson();
        AppInfo appInfo = gson.fromJson(jsonString, AppInfo.class);
        return appInfo;
    }
}

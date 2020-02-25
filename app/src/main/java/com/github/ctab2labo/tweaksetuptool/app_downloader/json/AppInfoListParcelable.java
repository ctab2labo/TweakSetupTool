package com.github.ctab2labo.tweaksetuptool.app_downloader.json;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class AppInfoListParcelable implements Parcelable {
    private ArrayList<AppInfo> appInfoList;

    public AppInfoListParcelable(ArrayList<AppInfo> appInfoList) {
        this.appInfoList = appInfoList;
    }

    private AppInfoListParcelable(Parcel in) {
        appInfoList = in.createTypedArrayList(AppInfo.CREATOR);
    }

    public ArrayList<AppInfo> getAppInfoList() {
        return appInfoList;
    }

    public void setAppInfoList(ArrayList<AppInfo> appInfoList) {
        this.appInfoList = appInfoList;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(appInfoList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AppInfoListParcelable> CREATOR = new Creator<AppInfoListParcelable>() {
        @Override
        public AppInfoListParcelable createFromParcel(Parcel in) {
            return new AppInfoListParcelable(in);
        }

        @Override
        public AppInfoListParcelable[] newArray(int size) {
            return new AppInfoListParcelable[size];
        }
    };
}

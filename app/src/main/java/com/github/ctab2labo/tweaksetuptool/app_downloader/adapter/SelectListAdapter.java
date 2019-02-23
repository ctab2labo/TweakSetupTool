package com.github.ctab2labo.tweaksetuptool.app_downloader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.github.ctab2labo.tweaksetuptool.R;

import java.util.ArrayList;

public class SelectListAdapter extends BaseAdapter {
    private ArrayList<AppPackageCheck> appPackageCheckList;
    private LayoutInflater layoutInflater;

    public SelectListAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }


    public void setAppPackageCheckList(ArrayList<AppPackageCheck> appPackageCheckList) {
        this.appPackageCheckList = appPackageCheckList;
    }

    @Override
    public int getCount() {
        return appPackageCheckList.size();
    }

    @Override
    public Object getItem(int position) {
        return appPackageCheckList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return appPackageCheckList.get(position).getId();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = layoutInflater.inflate(R.layout.list_view_select_app, parent, false);
        ((TextView) view.findViewById(R.id.list_view_select_app_name)).setText(appPackageCheckList.get(position).getName());
        ((TextView) view.findViewById(R.id.list_view_select_app_url)).setText(appPackageCheckList.get(position).getUrl() + "\n" + appPackageCheckList.get(position).getSummary());
        ((CheckBox) view.findViewById(R.id.list_view_select_app_check)).setChecked(appPackageCheckList.get(position).isChecked());
        ((CheckBox) view.findViewById(R.id.list_view_select_app_check)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppPackageCheck appPackageCheck = appPackageCheckList.get(position);
                appPackageCheck.setChecked(isChecked);
                appPackageCheckList.set(position, appPackageCheck);
            }
        });
        return view;
    }
}

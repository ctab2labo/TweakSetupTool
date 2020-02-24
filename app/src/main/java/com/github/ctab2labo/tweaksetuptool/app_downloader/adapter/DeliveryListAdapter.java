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
import com.github.ctab2labo.tweaksetuptool.app_downloader.json.AppInfo;
import com.github.ctab2labo.tweaksetuptool.app_downloader.json.DeliveryList;

import java.util.ArrayList;

public class DeliveryListAdapter extends BaseAdapter {
    private final ArrayList<AppInfo> appInfoList;
    private final Context context;
    private final LayoutInflater inflater;

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = null;

    public DeliveryListAdapter(Context context, DeliveryList deliveryList) {
        this.context = context;
        this.appInfoList = deliveryList.getAppList();
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return appInfoList.size();
    }

    @Override
    public Object getItem(int i) {
        return appInfoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public boolean isAnyChecked() {
        for (AppInfo appInfo : appInfoList) {
            if (appInfo.isChecked()) {
                return true;
            }
        }
        return false;
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    /**
     * リスナーに変更を通知する。
     */
    private void onCheckedChange(CompoundButton compoundButton, boolean b) {
        if (onCheckedChangeListener != null) {
            onCheckedChangeListener.onCheckedChanged(compoundButton, b);
        }
    }

    public ArrayList<AppInfo> getCheckedAppList() {
        ArrayList<AppInfo> list = new ArrayList<>();
        for (AppInfo appInfo : appInfoList) {
            if (appInfo.isChecked()) {
                list.add(appInfo);
            }
        }
        return list;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (view == null) {
            // ホールだーを使って負荷軽減
            view = inflater.inflate(R.layout.list_select_app, viewGroup, false);
            holder = new ViewHolder();
            holder.title = (TextView) view.findViewById(R.id.title);
            holder.description = (TextView) view.findViewById(R.id.description);
            holder.checkBox = (CheckBox) view.findViewById(R.id.check);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final AppInfo appInfo = appInfoList.get(i);
        holder.title.setText(appInfo.getName());
        holder.description.setText(appInfo.getSummary());
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                appInfo.setChecked(b);
                appInfoList.set(i, appInfo);
                DeliveryListAdapter.this.onCheckedChange(compoundButton, b); // 親のアダプターにも通知
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.checkBox.setChecked(! holder.checkBox.isChecked());
            }
        });
        holder.checkBox.setChecked(appInfo.isChecked());
        return view;
    }

    private static class ViewHolder {
        private TextView title;
        private TextView description;
        private CheckBox checkBox;
    }
}

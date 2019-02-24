package com.github.ctab2labo.tweaksetuptool.app_downloader.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.github.ctab2labo.tweaksetuptool.Common;
import com.github.ctab2labo.tweaksetuptool.R;
import com.github.ctab2labo.tweaksetuptool.app_downloader.adapter.AppPackageCheck;
import com.github.ctab2labo.tweaksetuptool.app_downloader.adapter.SelectListAdapter;
import com.github.ctab2labo.tweaksetuptool.app_downloader.json.AppPackage;

import java.util.ArrayList;

public class ChooseAppFragment extends Fragment {
    public static final String EXTRA_LIST_VERSION = "extra_list_version"; // int
    public static final String EXTRA_APP_PACKAGE_LIST = "extra_app_package_list"; // Serialize
    private ArrayList<AppPackage> appPackageList;
    private int listVersion;

    private ArrayList<AppPackageCheck> appPackageCheckList;
    private SelectListAdapter adapter;

    private ListView listView;
    private TextView textListVersion;
    private Button buttonStartDownload;
    private Button buttonSelectAll;
    private Button buttonNotSelectAll;

    private OnButtonClickListener onButtonClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_downloader_choose_app, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textListVersion = (TextView) view.findViewById(R.id.text_list_version);
        listView = (ListView) view.findViewById(R.id.list_view_select_app);
        buttonStartDownload = (Button) view.findViewById(R.id.button_start_download);
        buttonSelectAll = (Button) view.findViewById(R.id.button_select_all);
        buttonNotSelectAll = (Button) view.findViewById(R.id.button_not_select_all);
        buttonSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // すべての項目を選択
                for (int i=0;i<appPackageCheckList.size();i++){
                    AppPackageCheck appPackageCheck = appPackageCheckList.get(i);
                    appPackageCheck.setChecked(true);
                    appPackageCheckList.set(i, appPackageCheck);
                }
                adapter.notifyDataSetChanged();
            }
        });
        buttonNotSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // すべての項目を未選択
                for (int i=0;i<appPackageCheckList.size();i++){
                    AppPackageCheck appPackageCheck = appPackageCheckList.get(i);
                    appPackageCheck.setChecked(false);
                    appPackageCheckList.set(i, appPackageCheck);
                }
                adapter.notifyDataSetChanged();

            }
        });
        buttonStartDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<AppPackage> checkedAppPackages = getCheckedAppPackages();

                if (checkedAppPackages.size() == 0) {
                    new AlertDialog.Builder(getActivity())
                            .setMessage(R.string.error_nothing_checked)
                            .setPositiveButton(R.string.ok, null)
                            .show();
                } else {
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onButtonClick(checkedAppPackages);
                    }
                }
            }
        });

        Bundle args = getArguments(); // 引数を取得
        if (args != null) { // 取得できたら
            appPackageList = (ArrayList<AppPackage>) args.getSerializable(EXTRA_APP_PACKAGE_LIST);
            if (appPackageList == null) { // nullだったら、ダイアログを表示してリターン
                Common.DialogMakeHelper.showUnknownErrorDialog(getActivity(), "appPackageList is null.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ChooseAppFragment.this.getActivity().finish();
                    }
                });
                return;
            }

            // アダプターをセット
            appPackageCheckList = toCheckList(appPackageList);
            adapter = new SelectListAdapter(getActivity());
            adapter.setAppPackageCheckList(appPackageCheckList);
            listView.setAdapter(adapter);

            listVersion = args.getInt(EXTRA_LIST_VERSION);
            if (listVersion != 0) {
                textListVersion.setText(getString(R.string.text_list_version, listVersion));
            }
        } else { //　取得できなかった場合はダイアログを表示して終了
            Log.e(Common.TAG, "ChooseAppPreferenceFragment:getArguments() is null.");
            Common.DialogMakeHelper.showUnknownErrorDialog(getActivity(), "getArguments() is null.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ChooseAppFragment.this.getActivity().finish();
                }
            });
        }
    }
    // チェックされたパッケージだけを取得
    private ArrayList<AppPackage> getCheckedAppPackages() {
        ArrayList<AppPackage> appPackageArrayList = new ArrayList<>();
        for (AppPackageCheck appPackageCheck : appPackageCheckList) {
            if (appPackageCheck.isChecked()) {
                appPackageArrayList.add(appPackageCheck.getAppPackage());
            }
        }
        return appPackageArrayList;
    }

    // チェックリストに変換
    private ArrayList<AppPackageCheck> toCheckList(ArrayList<AppPackage> appPackageList) {
        ArrayList<AppPackageCheck> appPackageCheckList = new ArrayList<>();
        for (AppPackage appPackage : appPackageList) {
            appPackageCheckList.add(new AppPackageCheck(appPackage));
        }
        return appPackageCheckList;
    }

    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
    }

    public interface OnButtonClickListener {
        void onButtonClick(ArrayList<AppPackage> appPackageList);
    }
}

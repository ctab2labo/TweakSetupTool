package com.github.ctab2labo.tweaksetuptool.app_downloader.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.ctab2labo.tweaksetuptool.Common;
import com.github.ctab2labo.tweaksetuptool.R;
import com.github.ctab2labo.tweaksetuptool.app_downloader.json.AppPackage;

import java.util.ArrayList;

public class ChooseAppFragment extends Fragment {
    public static final String EXTRA_APP_PACKAGE_LIST = "extra_app_package_list";
    private ArrayList<AppPackage> appPackageList;
    private ChooseAppPreferenceFragment childPreferenceFragment;

    private Button buttonStartDownload;

    private OnButtonClickListener onButtonClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_downloader_choose_app, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        buttonStartDownload = (Button) view.findViewById(R.id.button_start_donwload);
        buttonStartDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<AppPackage> checkedAppPackages = childPreferenceFragment.getCheckedAppPackages();
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

        appPackageList = (ArrayList<AppPackage>) getArguments().getSerializable(EXTRA_APP_PACKAGE_LIST);
        // ないとは思うが、もしリストがnullだったらからのリストを作ってしのぐ。
        if (appPackageList == null) {
            Log.d(Common.TAG, "ChooseAppFragment:appPackageList is null.");
            appPackageList = new ArrayList<>();
        }

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        childPreferenceFragment = new ChooseAppPreferenceFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ChooseAppPreferenceFragment.EXTRA_APP_PACKAGE_LIST,appPackageList);
        childPreferenceFragment.setArguments(bundle);
        transaction.replace(R.id.layout_fragment_app_download, childPreferenceFragment);
        transaction.commit();
    }

    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
    }

    public interface OnButtonClickListener {
        void onButtonClick(ArrayList<AppPackage> appPackageList);
    }
}

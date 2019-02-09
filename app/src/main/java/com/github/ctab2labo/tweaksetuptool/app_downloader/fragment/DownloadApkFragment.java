package com.github.ctab2labo.tweaksetuptool.app_downloader.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ctab2labo.tweaksetuptool.Common;
import com.github.ctab2labo.tweaksetuptool.R;
import com.github.ctab2labo.tweaksetuptool.app_downloader.adapter.AppPackagePlus;
import com.github.ctab2labo.tweaksetuptool.app_downloader.adapter.DownloadListAdapter;
import com.github.ctab2labo.tweaksetuptool.app_downloader.json.AppPackage;
import com.github.ctab2labo.tweaksetuptool.app_downloader.service.DownloadApkService;

import java.io.File;
import java.util.ArrayList;

// Apkをダウンロードしているときに表示するフラグメント
public class DownloadApkFragment extends Fragment {
    public static final String EXTRA_APP_PACKAGE_LIST = "extra_app_package_list";

    private final int FLAG_NEW_LIST = 0;
    private final int FLAG_REBIND = 1;
    private final int REQUEST_APP_INSTALL = 1;

    private ListView listView;
    private TextView totalPercent;
    private TextView text;
    private ProgressBar totalBar;
    private Button buttonCancel;

    private ArrayList<AppPackage> appPackageList;
    private ArrayList<AppPackagePlus> appPackagePlusList;
    private DownloadListAdapter adapter;
    private int bindFlag;
    private int count;

    private int max;

    private DownloadApkService service;
    private final ServiceConnection downloadApkServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            service = ((DownloadApkService.DownloadApkServiceBinder) iBinder).getService();
            if (bindFlag == FLAG_REBIND) {
                appPackageList = service.getAppPackageList();
            }

            createView();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            service = null;
        }
    };

    private ArrayList<File> downloadedFiles;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_download_apk, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = (ListView) view.findViewById(R.id.list_view_download_apk);
        totalPercent = (TextView) view.findViewById(R.id.text_download_apk_percent);
        text = (TextView) view.findViewById(R.id.text_download_apk_text);
        totalBar = (ProgressBar) view.findViewById(R.id.bar_download_apk);
        buttonCancel = (Button) view.findViewById(R.id.button_download_apk_cancel);

        // ないとは思うが、もしリストがnullだったらサービスからリストを取得
        if (getArguments().containsKey(EXTRA_APP_PACKAGE_LIST)) {
            bindFlag = FLAG_NEW_LIST;
            appPackageList = (ArrayList<AppPackage>) getArguments().getSerializable(EXTRA_APP_PACKAGE_LIST);
            DownloadApkService.startDownloadService(getActivity(), appPackageList);
            Log.d(Common.TAG, String.valueOf(DownloadApkService.bindDownloadService(getActivity(), downloadApkServiceConnection)));
        } else {
            bindFlag = FLAG_REBIND;
            // サービスが起動していないなどの理由でバインドできなかった場合
            if (! DownloadApkService.bindDownloadService(getActivity(), downloadApkServiceConnection)) {
                Log.d(Common.TAG, "DownloadApkFragment:appPackageList is null.");
                getActivity().finish(); // とりあえずはアクティビティを終了する。後に変更あり
            }
        }
    }

    private void createView() {
        count = 0;
        appPackagePlusList = listToPlusList(appPackageList);
        adapter = new DownloadListAdapter(getActivity());
        adapter.setAppPackageList(appPackagePlusList);
        listView.setAdapter(adapter);
        setProgressMax(appPackageList.size() * 100);

        // テキスト表示を更新
        text.setText(getString(R.string.text_download_app,appPackagePlusList.get(count).getTitle(),count+1,appPackagePlusList.size()));

        service.addOnProgressUpdateListener(new DownloadApkService.OnProgressUpdateListener() {
            @Override
            public void onProgressUpdate(int index, int progress) {
                setProgressBar(index * 100 + progress);
                AppPackagePlus appPackagePlus = appPackagePlusList.get(index);
                appPackagePlus.setPercent(progress);
                appPackagePlusList.set(index, appPackagePlus);
                adapter.notifyDataSetChanged();
            }
        });
        service.addOnDownloadedListener(new DownloadApkService.OnDownloadedListener() {
            @Override
            public void onDownloaded(int index) {
                count++; // カウントアップ
                if (count < appPackagePlusList.size()) { // まだまだダウンロードするものがある場合
                    text.setText(getString(R.string.text_download_app,appPackagePlusList.get(count).getTitle(),count+1,appPackagePlusList.size()));
                }
            }
        });
        service.addOnCompletedListener(downloadCompletedListener);
        service.addOnDownloadFailedListener(new DownloadApkService.OnDownloadFailedListener() {
            @Override
            public void onDownloadFailed(Exception e) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.dialog_download_app_title))
                        .setMessage(getString(R.string.dialog_download_app_message, e.toString()))
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getActivity().finish(); // とりあえずはフィニッシュ。後に変更あり。
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                service.cancel();
                getActivity().finish(); // とりあえずはフィニッシュ。後に変更あり。
            }
        });
    }

    private DownloadApkService.OnCompletedListener downloadCompletedListener = new DownloadApkService.OnCompletedListener() {
        @Override
        public void onCompleted(ArrayList<File> downloadedFiles) {
            DownloadApkFragment.this.downloadedFiles = downloadedFiles;
            count = -1;
            installNextApp();
        }
    };

    private void installNextApp() {
        count++;
        setProgressBar(count);
        if (count < downloadedFiles.size()) { // まだまだインストールするものがある場合
            text.setText(getString(R.string.text_install_app, downloadedFiles.get(count).getName(), count + 1, downloadedFiles.size()));
            showAppInstall(downloadedFiles.get(count));
        } else {
            count = -1;
            getActivity().finish(); // とりあえずは終了
            for (File file : downloadedFiles) {
                if (file.delete()) downloadedFiles.remove(file);
            }
        }
    }

    private void showAppInstall(File file) {
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setData(Uri.fromFile(file));
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);

        startActivityForResult(intent, REQUEST_APP_INSTALL);
        Toast toast = Toast.makeText(getActivity(), R.string.toast_install_app, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 150);
        toast.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_APP_INSTALL) {
            if (resultCode == Activity.RESULT_OK) {
                installNextApp();
            } else if (resultCode == Activity.RESULT_FIRST_USER) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.dialog_cancel_install_title)
                        .setMessage(R.string.dialog_cancel_install_message)
                        .setPositiveButton(R.string.dialog_cancel_positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                installNextApp();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getActivity().finish();
                            }
                        })
                        .show();
            } else {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.dialog_cancel_install_title)
                        .setMessage(R.string.dialog_cancel_install_message)
                        .setPositiveButton(R.string.dialog_cancel_positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                installNextApp();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getActivity().finish();
                            }
                        })
                        .show();
            }
        }
    }

    private void setProgressMax(int i) {
        max = i;
    }

    private void setProgressBar(int i) {
        i = i * 100 / max;
        if (i > 100) i = 100;
        totalBar.setProgress(i);
        totalPercent.setText(String.valueOf(i) + "%");
    }

    private ArrayList<AppPackagePlus> listToPlusList(ArrayList<AppPackage> appPackageArrayList) {
        // パッケージオブジェクトをプラス版に変換
        ArrayList<AppPackagePlus> appPackagePlusArrayList = new ArrayList<>();
        for (AppPackage appPackage : appPackageArrayList) {
            appPackagePlusArrayList.add(AppPackagePlus.toPlus(appPackage));
        }
        return appPackagePlusArrayList;
    }
}

package com.github.ctab2labo.tweaksetuptool.app_downloader.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.ctab2labo.tweaksetuptool.Common;
import com.github.ctab2labo.tweaksetuptool.R;
import com.github.ctab2labo.tweaksetuptool.app_downloader.adapter.AppPackagePercent;
import com.github.ctab2labo.tweaksetuptool.app_downloader.adapter.DownloadListAdapter;
import com.github.ctab2labo.tweaksetuptool.app_downloader.adapter.DownloadedFile;
import com.github.ctab2labo.tweaksetuptool.app_downloader.json.AppPackage;
import com.github.ctab2labo.tweaksetuptool.app_downloader.service.DownloadApkService;

import java.io.File;
import java.util.ArrayList;

// Apkをダウンロードしているときに表示するフラグメント
public class DownloadApkFragment extends Fragment {
    public static final String EXTRA_APP_PACKAGE_LIST = "extra_app_package_list";

    private ListView listView;
    private TextView totalPercent;
    private TextView text;
    private ProgressBar totalBar;
    private Button buttonCancel;

    private ArrayList<AppPackage> appPackageList;
    private ArrayList<AppPackagePercent> appPackagePercentList;
    private DownloadListAdapter adapter;
    private int count;

    private int max;

    private OnDownloadCompletedListener onDownloadCompletedListener;

    private DownloadApkService service;

    private boolean isLoadedPercent = false;
    private final ServiceConnection downloadApkServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            service = ((DownloadApkService.DownloadApkServiceBinder) iBinder).getService();
            // アプリ側からアプリリストを取得し、ビューを作成
            appPackageList = service.getAppPackageList();
            createView();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            service = null;
        }
    };

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
        if (getArguments() != null) {
            appPackageList = (ArrayList<AppPackage>) getArguments().getSerializable(EXTRA_APP_PACKAGE_LIST);
            if (appPackageList != null) {
                DownloadApkService.startDownloadService(getActivity(), appPackageList);
                Log.e(Common.TAG, String.valueOf(DownloadApkService.bindDownloadService(getActivity(), downloadApkServiceConnection)));
            }
        }

        // あとはonResumeに任せる
    }

    // ビューを初期化
    private void createView() {
        count = 0;
        appPackagePercentList = listToPercentList(appPackageList);
        adapter = new DownloadListAdapter(getActivity());
        adapter.setAppPackageList(appPackagePercentList);
        listView.setAdapter(adapter);
        setProgressMax(appPackageList.size() * 100);

        // テキスト表示を更新
        text.setText(getString(R.string.text_download_app, appPackagePercentList.get(count).getName(),count+1, appPackagePercentList.size()));

        service.addOnProgressUpdateListener(onProgressUpdateListener);
        service.addOnDownloadedListener(onDownloadedListener);
        service.addOnCompletedListener(onCompletedListener);
        service.addOnDownloadFailedListener(onDownloadFailedListener);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                service.cancel();
                getActivity().finish(); // とりあえずはフィニッシュ。後に変更あり。
            }
        });
    }

    private DownloadApkService.OnProgressUpdateListener onProgressUpdateListener = new DownloadApkService.OnProgressUpdateListener() {
        @Override
        public void onProgressUpdate(int index, int progress) {
            loadPercent(index);
            setProgressBar(index * 100 + progress);
            AppPackagePercent appPackagePercent = appPackagePercentList.get(index);
            appPackagePercent.setPercent(progress);
            appPackagePercentList.set(index, appPackagePercent);
            adapter.notifyDataSetChanged();
        }
    };

    private DownloadApkService.OnDownloadedListener onDownloadedListener = new DownloadApkService.OnDownloadedListener() {
        @Override
        public void onDownloaded(int index) {
            count = index + 1; // カウントアップ
            for (int i=0;i<index;i++) {
                AppPackagePercent appPackagePercent = appPackagePercentList.get(i);
                appPackagePercent.setPercent(100);
                appPackagePercentList.set(i, appPackagePercent);
                adapter.notifyDataSetChanged();
            }
            if (count < appPackagePercentList.size()) { // まだまだダウンロードするものがある場合
                text.setText(getString(R.string.text_download_app, appPackagePercentList.get(count).getName(),count+1, appPackagePercentList.size()));
            }
        }
    };

    private DownloadApkService.OnCompletedListener onCompletedListener = new DownloadApkService.OnCompletedListener() {
        @Override
        public boolean onCompleted(ArrayList<File> downloadedFiles) {
            ArrayList<DownloadedFile> downloadedFileList = DownloadedFile.fileWithAppPackagePlusListToDownloadedFileList(appPackagePercentList, downloadedFiles);
            downloadCompleted(downloadedFileList);
            return true;
        }
    };

    private DownloadApkService.OnDownloadFailedListener onDownloadFailedListener = new DownloadApkService.OnDownloadFailedListener() {
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
    };

    @Override
    public void onPause() {
        super.onPause();
        if (service != null) { // サービスから切断
            service.removeOnProgressUpdateListener(onProgressUpdateListener);
            service.removeOnDownloadedListener(onDownloadedListener);
            service.removeOnCompletedListener(onCompletedListener);
            service.removeOnDownloadFailedListener(onDownloadFailedListener);
            getActivity().unbindService(downloadApkServiceConnection);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // サービスが起動していないなどの理由でバインドできなかった場合
        if (! DownloadApkService.bindDownloadService(getActivity(), downloadApkServiceConnection)) {
            Log.e(Common.TAG, "DownloadApkFragment:could't bind service.");
            Common.DialogMakeHelper.showUnknownErrorDialog(getActivity(), "could't bind service." + (appPackageList == null ? "\nappPackageList is null." : ""), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DownloadApkFragment.this.getActivity().finish();
                }
            });
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

    // アクティビティへ戻ってきたときに、ダウンロード中以外のファイルがすべて0%に戻っている不具合の一時しのぎのためのメソッド。のちに削除予定
    private void loadPercent(int index) {
        if (! isLoadedPercent) {
            for (int i=0;i<index;i++) {
                AppPackagePercent appPackagePercent = appPackagePercentList.get(i);
                appPackagePercent.setPercent(100);
                appPackagePercentList.set(i, appPackagePercent);
                adapter.notifyDataSetChanged();
            }
            isLoadedPercent = true;
        }
    }

    /**
     * アップパッケージリストをアップパッケージ＋リストに変換します。
     * @param appPackageArrayList アップパッケージリスト
     * @return 変換したプラスリスト
     */
    private ArrayList<AppPackagePercent> listToPercentList(ArrayList<AppPackage> appPackageArrayList) {
        // パッケージオブジェクトをプラス版に変換
        ArrayList<AppPackagePercent> appPackagePercentArrayList = new ArrayList<>();
        for (AppPackage appPackage : appPackageArrayList) {
            appPackagePercentArrayList.add(new AppPackagePercent(appPackage));
        }
        return appPackagePercentArrayList;
    }

    private void downloadCompleted(ArrayList<DownloadedFile> downloadedFileList) {
        if (onDownloadCompletedListener != null) {
            onDownloadCompletedListener.onDownloadCompleted(downloadedFileList);
        }
    }

    public void setOnDownloadCompletedListener(OnDownloadCompletedListener onDownloadCompletedListener) {
        this.onDownloadCompletedListener = onDownloadCompletedListener;
    }

    public interface OnDownloadCompletedListener {
        void onDownloadCompleted(ArrayList<DownloadedFile> downloadedFileList);
    }
}

package com.github.ctab2labo.tweaksetuptool.app_downloader.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ctab2labo.tweaksetuptool.Common;
import com.github.ctab2labo.tweaksetuptool.R;
import com.github.ctab2labo.tweaksetuptool.app_downloader.adapter.DownloadedFile;
import com.github.ctab2labo.tweaksetuptool.app_downloader.adapter.InstallListAdapter;

import java.io.File;
import java.util.ArrayList;

public class InstallApkFragment extends Fragment {
    public static final String BUNDLE_DOWNLOADED_FILES = "bundle_downloaded_files";
    private final int REQUEST_APP_INSTALL = 1;
    private ArrayList<DownloadedFile> downloadedFileList;
    private InstallListAdapter installListAdapter;

    private ListView listView;
    private TextView text;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_install_apk, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        boolean tempFlag = false;

        // 引数のDownloadedFileListがあるか調べる
        if (getArguments() != null) {
            downloadedFileList = (ArrayList<DownloadedFile>) getArguments().getSerializable(BUNDLE_DOWNLOADED_FILES);
            if (downloadedFileList == null) {
                Log.d(Common.TAG, "InstallApkFragment:downloadedFileList is null.");
                Common.DialogMakeHelper.showUnknownErrorDialog(getActivity(), "downloadedFileList is null.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        InstallApkFragment.this.getActivity().finish();
                    }
                });
                return;
            } // elseならそのまましたへ
        } else {
            Log.d(Common.TAG, "InstallApkFragment:downloadedFileList is null.");
            Common.DialogMakeHelper.showUnknownErrorDialog(getActivity(), "downloadedFileList is null.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    InstallApkFragment.this.getActivity().finish();
                }
            });
            return;
        }

        listView = (ListView) view.findViewById(R.id.list_view_install_apk);
        text = (TextView) view.findViewById(R.id.text_install_apk);

        /*
         * installNextAppは、最初のオブジェクトを消してしまうため、
         * 最初のアプリもインストールされるように空のオブジェクトを作っておく。
         */
        downloadedFileList.add(0, null);

        installListAdapter = new InstallListAdapter(getActivity());
        installListAdapter.setDownloadedFileList(downloadedFileList);
        listView.setAdapter(installListAdapter);
        installNextApp();
    }

    // 実行するごとに次のアプリがインストールされる
    private void installNextApp() {
        downloadedFileList.remove(0); // 以前のオブジェクトは削除
        if (downloadedFileList.size() != 0) {
            DownloadedFile downloadedFile = downloadedFileList.get(0);
            downloadedFile.setEnabledProgress(true);
            downloadedFileList.set(0, downloadedFile);
            installListAdapter.notifyDataSetChanged();
            text.setText(getString(R.string.text_installing, downloadedFileList.get(0).getTitle()));
            showAppInstall(new File(downloadedFileList.get(0).getPath()));
        } else {
            installListAdapter.notifyDataSetChanged();
            text.setText(R.string.text_all_success);

            // 完了メッセージを3秒表示して終了
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    InstallApkFragment.this.getActivity().finish();
                }
            }, 3000);
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
}

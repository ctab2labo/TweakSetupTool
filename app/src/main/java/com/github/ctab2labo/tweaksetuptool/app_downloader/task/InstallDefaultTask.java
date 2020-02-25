package com.github.ctab2labo.tweaksetuptool.app_downloader.task;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.github.ctab2labo.tweaksetuptool.BuildConfig;
import com.github.ctab2labo.tweaksetuptool.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InstallDefaultTask extends InstallBaseTask {
    private static final String BROADCAST_ACTION_INSTALL = BuildConfig.APPLICATION_ID + ".ACTION_INSTALL_COMMIT";

    private final Context context;
    private int sessionId;
    private final PackageInstaller mPackageInstaller;
    private File installFile;
    private BroadcastReceiver installationResultReceiver;
    private final Handler handler;

    public InstallDefaultTask(Context context, File installFile) {
        super(installFile);
        this.context = context;
        this.installFile = installFile;
        this.handler = new Handler();

        mPackageInstaller = context.getPackageManager().getPackageInstaller();
        installationResultReceiver = new InstallationResultReceiver(this);
    }

    @Override
    public void install() {
        // ブロードキャストレシーバーを登録することで、通知を受け取れるようにする。
        registerReceiver();

        // フリーズ防止
        Util.postAsync(new Runnable() {
            @Override
            public void run() {
                PackageInstaller.Session session = null;
                try {
                    PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
                    params.setInstallLocation(PackageInfo.INSTALL_LOCATION_AUTO);
                    sessionId = mPackageInstaller.createSession(params);
                    session = mPackageInstaller.openSession(sessionId);
                    writeFileToSession(installFile, session);
                    session.commit(getIntentSender(sessionId));
                } catch(IOException e) {
                    // これは一応メインで実行
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            failed();
                        }
                    });
                } finally {
                    Util.closeSilently(session);
                }
            }
        });
    }

    /**
     * パッケージインストーラーからのレシーバーを登録します。
     */
    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_ACTION_INSTALL);
        context.registerReceiver(installationResultReceiver, intentFilter);
    }

    /**
     * ファイルをインストーラーに書き込みます。
     */
    private void writeFileToSession(File file, PackageInstaller.Session session) throws IOException {
        InputStream in = context.getContentResolver().openInputStream(Uri.fromFile(file));
        OutputStream out = session.openWrite(file.getName(), 0, file.length());
        try {
            int c;
            byte[] buffer = new byte[65536];
            while ((c = in.read(buffer)) != -1) {
                out.write(buffer, 0, c);
            }
            session.fsync(out);
        } finally {
            Util.closeSilently(in);
            Util.closeSilently(out);
        }
    }

    private IntentSender getIntentSender(int sessionId) {
        return PendingIntent.getBroadcast(
                context,
                sessionId,
                new Intent(BROADCAST_ACTION_INSTALL),
                0
        ).getIntentSender();
    }

    private void processResult(Intent intent) {
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999);
        switch (status) {
            case PackageInstaller.STATUS_PENDING_USER_ACTION:
                Intent confirmationIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                startInstallerActivity(confirmationIntent);
                break;
            case PackageInstaller.STATUS_SUCCESS:
                successful();
                break;
            default:
                failed();
                break;
        }
    }

        private void startInstallerActivity(Intent installerActivity) {
            installerActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(installerActivity);
            } catch (Exception e) {
                failed();
            }
        }

    @Override
    protected void failed() {
        cleanSession();
        super.failed();
    }

    @Override
    protected void successful() {
        cleanSession();
        super.successful();
    }

    private void cleanSession() {
        context.unregisterReceiver(installationResultReceiver);
        try {
            mPackageInstaller.abandonSession(sessionId);
        } catch (Exception e) {
            Log.w(getClass().getSimpleName(), "Unable to abandon session", e);
        }
    }

    private static class InstallationResultReceiver extends BroadcastReceiver {
        private final InstallDefaultTask installDefaultTask;

        private InstallationResultReceiver(InstallDefaultTask installDefaultTask) {
            this.installDefaultTask = installDefaultTask;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            installDefaultTask.processResult(intent);
        }
    }
}

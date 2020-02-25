package com.github.ctab2labo.tweaksetuptool;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public final class Util {
    private Util() {}

    public static final String TAG = "TweakSetupTool";

    /**
     * インプットストリームから内容をすべて読み取り、バイト列を返します。
     * @param stream　読み取るインプットストリーム
     * @return 読み取ったバイト列
     * @throws IOException 読み取りに失敗した場合に捨てられます。
     */
    public static byte[] readAll(FileInputStream stream) throws IOException {
        ByteArrayOutputStream arrayOutput = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while (true) {
            len = stream.read(buffer);
            if (len < 0) {
                break;
            }
            arrayOutput.write(buffer, 0, len);
        }
        return arrayOutput.toByteArray();
    }

    public static File getExternalTempDir(Context context) {
        return context.getExternalCacheDir();
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {/* 無視 */}
        }
    }

    public static void postAsync(Runnable runnable) {
        new Thread(runnable).start();
    }

    /**
     * ダブらないように一時ファイルを作成します。
     * @param extension ピリオドなしの拡張子
     * @return ファイル名
     */
    public static File createTempFile(Context context, String extension) {
        File tempDir = getExternalTempDir(context);
        extension = "." + extension;
        int i = 0;
        File tempFile = new File(tempDir, i + extension);
        while(tempFile.exists()) {
            i++;
            tempFile = new File(tempDir, i + extension);
        }
        return tempFile;
    }

    public static final class AppDownloader {
        private AppDownloader() {}
        public static final String SHARED_PREFERENCE_KEY = "AppDownloader";
        public static final String KEY_LATEST_LIST_VERSION = "latest_list_version";
        public static final int NOTIFICATION_ID_DOWNLOADING = 1;
        public static final int NOTIFICATION_ID_DOWNLOADED = 2;
        public static final int NOTIFICATION_ID_LIST_UPDATE = 3;
    }

    public static final class SelfUpdate {
        private SelfUpdate() {}
        public static final int NOTIFICATION_ID_SELF_UPDATE = 4;
        public static final int PUBLIC_APP_VERSION = 200;
    }

    // 様々なダイアログを楽に表示します。
    public static final class DialogMakeHelper {
        private DialogMakeHelper() {}

        public static Dialog showUnknownErrorDialog(Context context) {
            return showUnknownErrorDialog(context, null, null);
        }

        public static Dialog showUnknownErrorDialog(Context context, String addMessage) {
            return showUnknownErrorDialog(context, addMessage, null);
        }

        public static Dialog showUnknownErrorDialog(Context context, DialogInterface.OnClickListener onClickListener) {
            return showUnknownErrorDialog(context, null, onClickListener);
        }

        public static Dialog showUnknownErrorDialog(Context context, String addMessage, DialogInterface.OnClickListener onClickListener) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.dialog_unknown_error_dialog_title);
            if (addMessage == null) {
                builder.setMessage(context.getString(R.string.dialog_unknown_error_dialog_message, ThreadUtils.calleedCallerFrom()));
            } else {
                builder.setMessage(context.getString(R.string.dialog_unknown_error_dialog_message_with, ThreadUtils.calleedCallerFrom(), addMessage));
            }
            builder.setPositiveButton(R.string.dialog_unknown_error_dialog_button, onClickListener);
            builder.setCancelable(false);
            return builder.show();
        }
    }

    public static final class ThreadUtils {
        private ThreadUtils() {}

        /**
         * 呼び出したメソッドのクラス名とメソッド名、行番号をjava.lang.Object.main(Object.java:2)のように表示
         *
         * @return  クラス名.メソッド名(ファイル名:行番号)
         */
        public static String calledAt() {
            StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
            return ste.toString();
        }

        /**
         * 呼び出したメソッドの内の読み出し元のクラス名とメソッド名、行番号をjava.lang.Object.main(Object.java:2)のように表示
         *
         * @return クラス名.メソッド名(ファイル名:行番号)
         */
        public static String callerFrom() {
            StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
            return ste.toString();
        }

        /**
         * 呼び出したメソッドの内の読み出し元のクラス名とメソッド名、行番号をjava.lang.Object.main(Object.java:2)のように表示
         *
         * @return クラス名.メソッド名(ファイル名:行番号)
         */
        public static String calleedCallerFrom() {
            StackTraceElement ste = Thread.currentThread().getStackTrace()[4];
            return ste.toString();
        }
    }
}

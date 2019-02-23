package com.github.ctab2labo.tweaksetuptool;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public final class Common {
    private Common() {}

    public static final String TAG = "TweakSetupTool";

    private static final String EXTERNAL_DIRECTORY_NAME = Common.class.getPackage().getName();
    public static final File EXTERNAL_SAVE_DIRECTORY = new File(Environment.getExternalStorageDirectory(), EXTERNAL_DIRECTORY_NAME);

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

    public static final class SystemUITweak {
        private SystemUITweak() {}
        public static final String SHARED_PREFERENCE_KEY = "SystemUITweak";
        public static final String KEY_ENABLED_KEEP_SERVICE = "enabled_keep_service";
    }

    public static final class AppDownloader {
        private AppDownloader() {}
        public static final String SHARED_PREFERENCE_KEY = "AppDownloader";
        public static final String KEY_LATEST_LIST_VERSION = "latest_list_version";
        public static final int NOTIFICATION_ID_DOWNLOADING = 1;
        public static final int NOTIFICATION_ID_DOWNLOADED = 2;
        public static final int NOTIFICATION_ID_LIST_UPDATE = 3;
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
                builder.setMessage(context.getString(R.string.dialog_unknown_error_dialog_message, ThreadUtils.calledFrom()));
            } else {
                builder.setMessage(context.getString(R.string.dialog_unknown_error_dialog_message_with, ThreadUtils.calledFrom(), addMessage));
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
        public static String calledFrom() {
            StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
            return ste.toString();
        }
    }
}

package com.github.ctab2labo.tweaksetuptool.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.github.ctab2labo.tweaksetuptool.R;
import com.github.ctab2labo.tweaksetuptool.json.AppPackage;
import com.github.ctab2labo.tweaksetuptool.json.DeliveryList;

import java.util.List;

// アプリリストからインストールアプリを選ぶためのダイアログクラス
public class ChoseListDialog extends AlertDialog.Builder {
    private OnCompleteListner completeListner;
    private OnCancelListner cancelListner;
    private List<AppPackage> packageList;
    private final List<AppPackage> defaultList;

    public ChoseListDialog(Context context, DeliveryList list) {
        super(context);
        this.setTitle(R.string.choose_dialog_title);
        packageList = list.app_list;
        defaultList = list.app_list;
        final String[] appStrings = new String[packageList.size()];

        // アプリ名を取り出して、選択肢用
        for (int i=0;i<packageList.size();i++) {
            appStrings[i] = packageList.get(i).name;
        }
        // boolsをすべてtrueに
        boolean[] bools = new boolean[appStrings.length];
        for (int i=0;i<bools.length;i++) {
            bools[i] = true;
        }
        this.setMultiChoiceItems(appStrings, bools, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which, boolean b) {
                if (b) {
                    packageList.add(defaultList.get(which));
                } else {
                    packageList.remove(defaultList.get(which));
                }
            }
        });
        this.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (completeListner != null) {
                    completeListner.onComplete(packageList.toArray(new AppPackage[0]));
                }
            }
        });
        this.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (cancelListner != null) {
                    cancelListner.onCancel();
                }
            }
        });
    }

    public void setCompleteListner(OnCompleteListner completeListner) {
        this.completeListner = completeListner;
    }

    public void setCancelListener(OnCancelListner cancelListener) {
        this.cancelListner = cancelListener;
    }

    public interface OnCompleteListner {
        void onComplete(AppPackage[] packagesList);
    }

    public interface OnCancelListner {
        void onCancel();
    }
}

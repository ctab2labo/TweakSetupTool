package com.github.ctab2labo.tweaksetuptool.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.github.ctab2labo.tweaksetuptool.R;
import com.github.ctab2labo.tweaksetuptool.json.AppPackage;

import java.util.ArrayList;
import java.util.List;

// アプリリストからインストールアプリを選ぶためのダイアログクラス
public class ChoseListDialog extends AlertDialog.Builder {
    private OnCompleteListner completeListner;
    private OnCancelListner cancelListner;
    private List<AppPackage> packageList;
    private boolean[] checks;

    public ChoseListDialog(Context context, List<AppPackage> list) {
        super(context);
        this.setTitle(R.string.choose_dialog_title);
        packageList = list;
        String[] appStrings = new String[packageList.size()];

        // アプリ名を取り出して、選択肢用
        for (int i=0;i<packageList.size();i++) {
            appStrings[i] = packageList.get(i).name;
        }
        // boolsをすべてtrueに
        checks = new boolean[appStrings.length];
        for (int i=0;i<checks.length;i++) {
            checks[i] = true;
        }
        this.setMultiChoiceItems(appStrings, checks, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which, boolean b) {
                checks[which] = b;
            }
        });
        this.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (completeListner != null) {
                    List<AppPackage> checkedPackages = new ArrayList<>();
                    for (int j=0;j<checks.length;j++) {
                        if(checks[j]) {
                            checkedPackages.add(packageList.get(j));
                        }
                    }
                    completeListner.onComplete(checkedPackages.toArray(new AppPackage[0]));
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

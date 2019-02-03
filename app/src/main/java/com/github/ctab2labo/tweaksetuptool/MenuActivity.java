package com.github.ctab2labo.tweaksetuptool;

import android.app.Activity;
import android.os.Bundle;

import com.github.ctab2labo.tweaksetuptool.menu.fragment.MenuFragment;

public class MenuActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // 一時ディレクトリの作成
        if (! Common.SAVE_DIRECTORY.exists()) {
            Common.SAVE_DIRECTORY.mkdir();
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.layout_menu, new MenuFragment())
                .commit();
    }
}

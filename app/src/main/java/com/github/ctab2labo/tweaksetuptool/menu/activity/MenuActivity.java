package com.github.ctab2labo.tweaksetuptool.menu.activity;

import android.app.Activity;
import android.os.Bundle;

import com.github.ctab2labo.tweaksetuptool.R;
import com.github.ctab2labo.tweaksetuptool.menu.fragment.MenuFragment;

public class MenuActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        getFragmentManager().beginTransaction()
                .replace(R.id.layout_menu, new MenuFragment())
                .commit();
    }
}

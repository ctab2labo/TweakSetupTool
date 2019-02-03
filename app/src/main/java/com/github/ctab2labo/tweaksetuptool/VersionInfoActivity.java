package com.github.ctab2labo.tweaksetuptool;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

/*
 * 開発メモ：
 * Githubにコミットされていなかったクラスファイルのため、
 * 一時的にこのクラスを置くことにする。
 * コミットする場合はこのクラスファイルを削除すること
 * shiosefine
 */

public class VersionInfoActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_versioninfo);
        setTitle(R.string.activity_versioninfo_title);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

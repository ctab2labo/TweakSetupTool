package com.github.ctab2labo.tweaksetuptool;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.github.ctab2labo.tweaksetuptool.self_update.activity.SelfUpdateActivity;

/*
 * 開発メモ：
 * Githubにコミットされていなかったクラスファイルのため、
 * 一時的にこのクラスを置くことにする。
 * コミットする場合はこのクラスファイルを削除すること
 * shiosefine
 */

public class VersionInfoActivity extends Activity {
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_versioninfo);
        setTitle(R.string.activity_versioninfo_title);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VersionInfoActivity.this, SelfUpdateActivity.class);
                startActivity(intent);
            }
        });
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

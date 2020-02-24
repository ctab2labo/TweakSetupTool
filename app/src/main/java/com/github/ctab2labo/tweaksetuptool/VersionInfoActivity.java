package com.github.ctab2labo.tweaksetuptool;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/*
 * 開発メモ：
 * Githubにコミットされていなかったクラスファイルのため、
 * 一時的にこのクラスを置くことにする。
 * コミットする場合はこのクラスファイルを削除すること
 * shiosefine
 */

public class VersionInfoActivity extends Activity {
    private TextView discordLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_versioninfo);
        setTitle(R.string.activity_versioninfo_title);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        discordLink = (TextView) findViewById(R.id.text_discord);
        // HTML形式のリッチなテキストで表示
        discordLink.setText(Html.fromHtml(getString(R.string.text_discord)));
        discordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri urlDiscord = Uri.parse(getString(R.string.url_discord));
                Intent intent = new Intent(Intent.ACTION_VIEW, urlDiscord);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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

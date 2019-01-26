package com.github.ctab2labo.tweaksetuptool;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener{
    private TextView titleView;
    private ProgressBar progress;
    private TextView progressView;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titleView = (TextView) findViewById(R.id.title_view);
        progress = (ProgressBar) findViewById(R.id.progress);
        progressView = (TextView) findViewById(R.id.progress_view);
        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

    }
}

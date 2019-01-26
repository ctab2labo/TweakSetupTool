package com.github.ctab2labo.tweaksetuptool;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ctab2labo.tweaksetuptool.task.FileDownloadTask;

import java.io.File;

public class MainActivity extends Activity implements View.OnClickListener{
    private TextView titleView;
    private ProgressBar progress;
    private TextView progressView;
    private Button button;

    private int progressMax = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titleView = (TextView) findViewById(R.id.title_view);
        progress = (ProgressBar) findViewById(R.id.progress);
        progressView = (TextView) findViewById(R.id.progress_view);
        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(this);
        progress.setMax(100);
    }

    @Override
    public void onClick(View view) {
        button.setEnabled(false);
        titleView.setText(getString(R.string.text_download_list));
        setParcent(0);
        setProgressMax(1);
        FileDownloadTask task = new FileDownloadTask(getString(R.string.url_list), new File(getFilesDir(), "deliveryList.json"));
        task.setSuccessListner(urlListSuccessListner);
        task.execute();
    }

    private final FileDownloadTask.OnSuccessListner urlListSuccessListner = new FileDownloadTask.OnSuccessListner() {
        @Override
        public void onSuccess(boolean bool) {
            setParcent(1);

        }
    };

    private void setProgressMax(int max) {
        progressMax = max;
    }

    private void setParcent(int progress) {
        progress = progress * 100 / progressMax;
        if (progress > 100) progress = 100;
        this.progress.setProgress(progress);
        this.progressView.setText(new StringBuilder().append(progress).append("%"));
    }
}

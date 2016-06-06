package com.jasp.serviapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

public class InitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_init);
        // Ver https://developer.zendesk.com/embeddables/docs/android-chat-sdk/sessionapi

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(InitActivity.this, LoginActivity.class));
            }
        };

        // Simulate a long loading process on application startup.
        Timer timer = new Timer();
        timer.schedule(task, 1000);
    }
}

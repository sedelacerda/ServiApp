package com.jasp.serviapp;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Timer;
import java.util.TimerTask;

public class InitActivity extends AppCompatActivity {

    public static DatabaseReference myFirebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_init);

        myFirebaseRef = FirebaseDatabase.getInstance().getReference();

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

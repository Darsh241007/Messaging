package com.darsh.messaging;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

public class Splash_screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences=getSharedPreferences("file",MODE_PRIVATE);

                if (sharedPreferences.contains("first")){
                    Intent mainIntent=new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }
                else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("first","hello");
                    editor.apply();

                    Intent getStartedIntent=new Intent(getApplicationContext(),Get_started.class);
                    startActivity(getStartedIntent);
                    finish();

                }

            }
        },2000);
    }
}



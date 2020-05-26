package com.darsh.messaging;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

public class Get_started extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);
        Button get_started = findViewById(R.id.Button_getStarted);
        get_started.setOnClickListener(v -> {
            SharedPreferences sharedPreferences=getSharedPreferences("file",MODE_PRIVATE);

            if (sharedPreferences.contains("first")){
                Intent MainIntent=new Intent(getApplicationContext(),MainActivity.class);
                startActivity(MainIntent);
            }else {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("first","hello");
                editor.apply();
            }

        });

    }





    }


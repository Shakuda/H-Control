package com.stuffaboutcode.bluedot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class Welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

    }

    public void Start() {
        Intent intent = new Intent(Welcome.this,Devices.class);
        startActivity(intent);
    }
}

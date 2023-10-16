package com.example.beediotest1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class info extends AppCompatActivity {
    ImageView logoImageView;
    TextView infoTitle, info;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        setTitle("Information");
        logoImageView = findViewById(R.id.LogoImageView);
        infoTitle = findViewById(R.id.infoTitle);
        info = findViewById(R.id.info);
    }
}
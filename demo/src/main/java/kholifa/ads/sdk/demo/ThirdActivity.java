package kholifa.ads.sdk.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import kholifa.ads.sdk.AdNetwork;

public class ThirdActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);
        new AdNetwork(this).init();
    }
}
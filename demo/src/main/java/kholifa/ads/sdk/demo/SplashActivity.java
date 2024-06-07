package kholifa.ads.sdk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import kholifa.ads.sdk.AdConfig;
import kholifa.ads.sdk.AdNetwork;
import kholifa.ads.sdk.data.AdNetworkType;
import kholifa.ads.sdk.listener.AdOpenListener;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = AdNetwork.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        AdNetwork adNetwork = new AdNetwork(this);
        AdConfig.ad_inters_interval = 2;
        AdConfig.retry_from_start_max = 2;
        AdConfig.ad_networks = new AdNetworkType[]{
                AdNetworkType.APPLOVIN
        };
        adNetwork.init();
        //startActivity(new Intent(SplashActivity.this, MainActivity.class));
        adNetwork.loadAndShowOpenAppAd(this, true, new AdOpenListener() {
            @Override
            public void onFinish() {
                Log.d(TAG, "ON FINISH OPEN APP");
                finish();
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
        });
    }
}
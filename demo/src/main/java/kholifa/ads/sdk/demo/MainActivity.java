package kholifa.ads.sdk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import kholifa.ads.sdk.AdConfig;
import kholifa.ads.sdk.AdNetwork;
import kholifa.ads.sdk.data.AdNetworkType;
import kholifa.ads.sdk.listener.AdRewardedListener;

public class MainActivity extends AppCompatActivity {

    AdNetwork adNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adNetwork = new AdNetwork(this);
        adNetwork.loadShowUMPConsentForm();
        AdConfig.ad_inters_interval = 2;
        AdConfig.retry_from_start_max = 2;

        adNetwork.init();

        adNetwork.loadBannerAd(true, findViewById(R.id.banner_container));
        adNetwork.loadInterstitialAd(true);
        adNetwork.loadRewardedAd(true, new AdRewardedListener() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onDismissed() {

            }

            @Override
            public void onError() {

            }
        });



        ((Button) findViewById(R.id.banner_applovin)).setOnClickListener(view -> {
            AdConfig.ad_networks = new AdNetworkType[] { AdNetworkType.APPLOVIN };
            initAndLoad(adNetwork);
        });

        ((Button) findViewById(R.id.banner_applovin_disc)).setOnClickListener(view -> {
            AdConfig.ad_networks = new AdNetworkType[] { AdNetworkType.APPLOVIN_DISCOVERY };
            initAndLoad(adNetwork);
        });


        // Interstitial section -------------------------------------------------------------------

        ((Button) findViewById(R.id.inters_all)).setOnClickListener(view -> {
            adNetwork.showInterstitialAd(true);
        });

        ((Button) findViewById(R.id.openapp_all)).setOnClickListener(view -> {
            AdNetwork.showOpenAppAd(this, true);
        });

        ((Button) findViewById(R.id.rewarded_all)).setOnClickListener(view -> {
            adNetwork.showRewardedAd(true, new AdRewardedListener() {
                @Override
                public void onComplete() {
                    Toast.makeText(getApplicationContext(), "Rewarded complete", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onDismissed() {

                }

                @Override
                public void onError() {
                    Toast.makeText(getApplicationContext(), "Rewarded error", Toast.LENGTH_SHORT).show();
                }
            });
        });

        ((Button) findViewById(R.id.next_activity)).setOnClickListener(view -> {
            startActivity(new Intent(this, ThirdActivity.class));
        });

    }

    private void initAndLoad(AdNetwork adNetwork){
        adNetwork.init();
        adNetwork.loadBannerAd(true, findViewById(R.id.banner_container));
        adNetwork.loadInterstitialAd(true);
        adNetwork.loadRewardedAd(true, new AdRewardedListener() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onDismissed() {

            }

            @Override
            public void onError() {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adNetwork.destroyAndDetachBanner();
    }
}
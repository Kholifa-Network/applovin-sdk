package kholifa.ads.sdk.format;

import static kholifa.ads.sdk.AdConfig.ad_applovin_interstitial_unit_id;
import static kholifa.ads.sdk.AdConfig.ad_applovin_interstitial_zone_id;
import static kholifa.ads.sdk.AdConfig.ad_networks;
import static kholifa.ads.sdk.data.AdNetworkType.APPLOVIN;
import static kholifa.ads.sdk.data.AdNetworkType.APPLOVIN_DISCOVERY;
import static kholifa.ads.sdk.data.AdNetworkType.APPLOVIN_MAX;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.applovin.adview.AppLovinInterstitialAd;
import com.applovin.adview.AppLovinInterstitialAdDialog;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinSdk;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;

import java.util.concurrent.TimeUnit;

import kholifa.ads.sdk.AdConfig;
import kholifa.ads.sdk.AdNetwork;
import kholifa.ads.sdk.data.AdNetworkType;
import kholifa.ads.sdk.data.SharedPref;
import kholifa.ads.sdk.helper.AppLovinCustomEventInterstitial;
import kholifa.ads.sdk.listener.AdIntersListener;

public class InterstitialAdFormat {

    private static final String TAG = AdNetwork.class.getSimpleName();

    //Interstitial
    private com.google.android.gms.ads.interstitial.InterstitialAd adMobInterstitialAd;
    private AdManagerInterstitialAd adManagerInterstitialAd;
    private com.facebook.ads.InterstitialAd fanInterstitialAd;
    private MaxInterstitialAd maxInterstitialAd;
    public AppLovinInterstitialAdDialog appLovinInterstitialAdDialog;
    public AppLovinAd appLovinAd;

    private static int last_interstitial_index = 0;

    private final Activity activity;
    private final SharedPref sharedPref;
    private AdIntersListener listener;

    private int retryAttempt;
    private int counter = 1;

    public InterstitialAdFormat(Activity activity) {
        this.activity = activity;
        sharedPref = new SharedPref(activity);
    }

    public void loadInterstitialAd(int ad_index, int retry_count) {
        if (retry_count > AdConfig.retry_from_start_max) return;
        last_interstitial_index = ad_index;
        AdNetworkType type = ad_networks[ad_index];
        if (type == APPLOVIN || type == APPLOVIN_MAX) {
            maxInterstitialAd = new MaxInterstitialAd(ad_applovin_interstitial_unit_id, activity);
            maxInterstitialAd.setListener(new MaxAdListener() {
                @Override
                public void onAdLoaded(MaxAd ad) {
                    retryAttempt = 0;
                    Log.d(TAG, type.name() + " interstitial onAdLoaded");
                }

                @Override
                public void onAdDisplayed(MaxAd ad) {
                    sharedPref.setIntersCounter(0);
                }

                @Override
                public void onAdHidden(MaxAd ad) {
                    maxInterstitialAd.loadAd();
                }

                @Override
                public void onAdClicked(MaxAd ad) {

                }

                @Override
                public void onAdLoadFailed(String adUnitId, MaxError error) {
                    retryAttempt++;
                    long delayMillis = TimeUnit.SECONDS.toMillis((long) Math.pow(2, Math.min(6, retryAttempt)));
                    new Handler().postDelayed(() -> maxInterstitialAd.loadAd(), delayMillis);
                    Log.d(TAG, type.name() + " interstitial onAdLoadFailed");
                    retryLoadInterstitial(ad_index, retry_count);
                }

                @Override
                public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                    maxInterstitialAd.loadAd();
                }
            });

            // Load the first ad
            maxInterstitialAd.loadAd();
        } else if (type == APPLOVIN_DISCOVERY) {
            AdRequest.Builder builder = new AdRequest.Builder();
            Bundle interstitialExtras = new Bundle();
            interstitialExtras.putString("zone_id", ad_applovin_interstitial_zone_id);
            builder.addCustomEventExtrasBundle(AppLovinCustomEventInterstitial.class, interstitialExtras);
            AppLovinSdk.getInstance(activity).getAdService().loadNextAd(AppLovinAdSize.INTERSTITIAL, new AppLovinAdLoadListener() {
                @Override
                public void adReceived(AppLovinAd ad) {
                    Log.d(TAG, type.name() + " interstitial adReceived");
                    appLovinAd = ad;
                }

                @Override
                public void failedToReceiveAd(int errorCode) {
                    Log.d(TAG, type.name() + " interstitial failedToReceiveAd");
                    retryLoadInterstitial(ad_index, retry_count);
                }

            });
            appLovinInterstitialAdDialog = AppLovinInterstitialAd.create(AppLovinSdk.getInstance(activity), activity);
        }
    }

    public boolean showInterstitialAd() {
        int counter = sharedPref.getIntersCounter();
        Log.d(TAG, "COUNTER " + counter);

        if (counter <= AdConfig.ad_inters_interval) {
            Log.d(TAG, "COUNTER not-reach attempt : " + counter);
            sharedPref.setIntersCounter(sharedPref.getIntersCounter() + 1);
            return false;
        }

        Log.d(TAG, "COUNTER reach attempt");
        int ad_index = last_interstitial_index;
        AdNetworkType type = ad_networks[ad_index];
        if (type == APPLOVIN || type == APPLOVIN_MAX) {
            if (maxInterstitialAd == null || !maxInterstitialAd.isReady()) {
                loadInterstitialAd(0, 0);
                return false;
            }
            maxInterstitialAd.showAd();

        } else if (type == APPLOVIN_DISCOVERY) {
            if (appLovinInterstitialAdDialog == null) {
                loadInterstitialAd(0, 0);
                return false;
            }
            sharedPref.setIntersCounter(0);
            appLovinInterstitialAdDialog.showAndRender(appLovinAd);
        }
        return true;
    }

    public void setListener(AdIntersListener listener) {
        this.listener = listener;
    }

    private void retryLoadInterstitial(int ad_index, int retry_count) {
        int adIndex = ad_index + 1;
        int finalRetry = retry_count;
        if (adIndex > AdConfig.ad_networks.length - 1) {
            adIndex = 0;
            finalRetry++;
        }
        Log.d(TAG, "delayAndLoadInterstitial ad_index : " + ad_index + " retry_count : " + retry_count);
        final int _adIndex = adIndex, _finalRetry = finalRetry;
        new Handler(activity.getMainLooper()).postDelayed(() -> {
            loadInterstitialAd(_adIndex, _finalRetry);
        }, 3000);
    }

}

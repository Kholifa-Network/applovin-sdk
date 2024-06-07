package kholifa.ads.sdk.format;

import static kholifa.ads.sdk.AdConfig.ad_applovin_rewarded_unit_id;
import static kholifa.ads.sdk.AdConfig.ad_applovin_rewarded_zone_id;
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
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinSdk;
import com.google.android.gms.ads.AdRequest;

import kholifa.ads.sdk.AdConfig;
import kholifa.ads.sdk.AdNetwork;
import kholifa.ads.sdk.data.AdNetworkType;
import kholifa.ads.sdk.helper.AppLovinCustomEventInterstitial;
import kholifa.ads.sdk.listener.AdRewardedListener;

public class RewardAdFormat {

    private static final String TAG = AdNetwork.class.getSimpleName();

    private com.google.android.gms.ads.rewarded.RewardedAd adMobRewardedAd;
    private com.google.android.gms.ads.rewarded.RewardedAd adManagerRewardedAd;
    private com.facebook.ads.RewardedVideoAd fanRewardedVideoAd;
    private MaxRewardedAd applovinMaxRewardedAd;
    public AppLovinInterstitialAdDialog appLovinInterstitialAdDialog;
    public AppLovinAd appLovinAd;

    private static int last_reward_index = 0;

    private final Activity activity;

    public RewardAdFormat(Activity activity) {
        this.activity = activity;
    }

    public void loadRewardAd(int ad_index, int retry_count, AdRewardedListener listener) {
        if (retry_count > AdConfig.retry_from_start_max) return;
        last_reward_index = ad_index;
        AdNetworkType type = ad_networks[ad_index];
        Log.d(TAG, type.name() + " rewarded loadRewardAd");

        if (type == APPLOVIN || type == APPLOVIN_MAX) {
            applovinMaxRewardedAd = MaxRewardedAd.getInstance(ad_applovin_rewarded_unit_id, activity);
            applovinMaxRewardedAd.loadAd();
            applovinMaxRewardedAd.setListener(new MaxRewardedAdListener() {
                @Override
                public void onUserRewarded(MaxAd maxAd, MaxReward maxReward) {
                    Log.d(TAG, type.name() + " rewarded onComplete");
                    listener.onComplete();
                }

                @Override
                public void onRewardedVideoStarted(MaxAd maxAd) {

                }

                @Override
                public void onRewardedVideoCompleted(MaxAd maxAd) {
                    Log.d(TAG, type.name() + " rewarded onComplete");
                    listener.onComplete();
                }

                @Override
                public void onAdLoaded(MaxAd maxAd) {
                    Log.d(TAG, type.name() + " rewarded onAdLoaded");
                }

                @Override
                public void onAdDisplayed(MaxAd maxAd) {

                }

                @Override
                public void onAdHidden(MaxAd maxAd) {
                    Log.d(TAG, type.name() + " rewarded onAdHidden");
                    retryLoadReward(ad_index, retry_count, listener);
                    listener.onComplete();
                }

                @Override
                public void onAdClicked(MaxAd maxAd) {

                }

                @Override
                public void onAdLoadFailed(String s, MaxError maxError) {
                    Log.d(TAG, type.name() + " rewarded onAdLoadFailed " + maxError.getMessage());
                    retryLoadReward(ad_index, retry_count, listener);
                }

                @Override
                public void onAdDisplayFailed(MaxAd maxAd, MaxError maxError) {
                    Log.d(TAG, type.name() + " rewarded onAdDisplayFailed " + maxError.getMessage());
                    retryLoadReward(ad_index, retry_count, listener);
                }
            });
        } else if (type == APPLOVIN_DISCOVERY) {
            AdRequest.Builder builder = new AdRequest.Builder();
            Bundle interstitialExtras = new Bundle();
            interstitialExtras.putString("zone_id", ad_applovin_rewarded_zone_id);
            builder.addCustomEventExtrasBundle(AppLovinCustomEventInterstitial.class, interstitialExtras);
            AppLovinSdk.getInstance(activity).getAdService().loadNextAd(AppLovinAdSize.INTERSTITIAL, new AppLovinAdLoadListener() {
                @Override
                public void adReceived(AppLovinAd ad) {
                    Log.d(TAG, type.name() + " rewarded onAdLoaded");
                    appLovinAd = ad;
                }

                @Override
                public void failedToReceiveAd(int errorCode) {
                    Log.d(TAG, type.name() + " rewarded failedToReceiveAd " + errorCode);
                    retryLoadReward(ad_index, retry_count, listener);
                }
            });
            appLovinInterstitialAdDialog = AppLovinInterstitialAd.create(AppLovinSdk.getInstance(activity), activity);
            appLovinInterstitialAdDialog.setAdDisplayListener(new AppLovinAdDisplayListener() {
                @Override
                public void adDisplayed(AppLovinAd appLovinAd) {

                }

                @Override
                public void adHidden(AppLovinAd appLovinAd) {
                    Log.d(TAG, type.name() + " rewarded adHidden");
                    retryLoadReward(ad_index, retry_count, listener);
                    listener.onComplete();
                }
            });
        }
    }

    public boolean showRewardAd(AdRewardedListener listener) {
        AdNetworkType type = ad_networks[last_reward_index];
        if (type == APPLOVIN || type == APPLOVIN_MAX ) {
            if (applovinMaxRewardedAd != null && applovinMaxRewardedAd.isReady()) {
                applovinMaxRewardedAd.showAd();
            } else {
                listener.onError();
            }
        } else if (type == APPLOVIN_DISCOVERY) {
            if (appLovinInterstitialAdDialog != null) {
                appLovinInterstitialAdDialog.showAndRender(appLovinAd);
            } else {
                listener.onError();
            }
        }
        return true;
    }

    private void retryLoadReward(int ad_index, int retry_count, AdRewardedListener listener) {
        int adIndex = ad_index + 1;
        int finalRetry = retry_count;
        if (adIndex > AdConfig.ad_networks.length - 1) {
            adIndex = 0;
            finalRetry++;
        }
        Log.d(TAG, "delayAndLoadReward ad_index : " + ad_index + " retry_count : " + retry_count);
        final int _adIndex = adIndex, _finalRetry = finalRetry;
        new Handler(activity.getMainLooper()).postDelayed(() -> {
            loadRewardAd(_adIndex, _finalRetry, listener);
        }, 1000);
    }

}

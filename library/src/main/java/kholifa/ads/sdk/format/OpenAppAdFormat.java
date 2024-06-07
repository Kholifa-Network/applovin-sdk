package kholifa.ads.sdk.format;

import static kholifa.ads.sdk.AdConfig.ad_applovin_interstitial_zone_id;
import static kholifa.ads.sdk.AdConfig.ad_applovin_open_app_unit_id;
import static kholifa.ads.sdk.AdConfig.ad_networks;
import static kholifa.ads.sdk.AdConfig.ad_replace_unsupported_open_app_with_interstitial_on_splash;
import static kholifa.ads.sdk.AdConfig.retry_from_start_max;
import static kholifa.ads.sdk.data.AdNetworkType.APPLOVIN;
import static kholifa.ads.sdk.data.AdNetworkType.APPLOVIN_DISCOVERY;
import static kholifa.ads.sdk.data.AdNetworkType.APPLOVIN_MAX;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.applovin.adview.AppLovinInterstitialAd;
import com.applovin.adview.AppLovinInterstitialAdDialog;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAppOpenAd;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinSdk;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.Date;

import kholifa.ads.sdk.AdNetwork;
import kholifa.ads.sdk.data.AdNetworkType;
import kholifa.ads.sdk.helper.AppLovinCustomEventInterstitial;
import kholifa.ads.sdk.listener.ActivityListener;
import kholifa.ads.sdk.listener.AdOpenListener;

public class OpenAppAdFormat {

    private static final String TAG = AdNetwork.class.getSimpleName();

    private final Activity activity;

    private static boolean openAppSplashFinished = false;

    private static boolean isLoadingAd = false;
    public static boolean isShowingAd = false;
    private static long loadTime = 0;
    private static long lastShowTime = 0;

    public static int last_open_app_index = -1;
    public static AppOpenAd ad_admob_appOpenAd = null;
    public static com.google.android.gms.ads.appopen.AppOpenAd ad_manager_appOpenAd = null;
    public static MaxAppOpenAd ad_applovin_appOpenAd = null;
    private static ActivityListener activityListener = null;

    public OpenAppAdFormat(Activity activity) {
        this.activity = activity;
        initActivityListener(activity.getApplication());
    }

    public static void initActivityListener(Application application) {
        activityListener = new ActivityListener(application);
    }

    public void loadAndShowOpenAppAd(int ad_index, int retry_count, AdOpenListener listener) {
        if (retry_count > retry_from_start_max) {
            openAppSplashFinish(listener);
            return;
        }

        AdNetworkType type = ad_networks[ad_index];
        if (type == APPLOVIN || type == APPLOVIN_MAX) {
            MaxAppOpenAd maxAppOpenAd = new MaxAppOpenAd(ad_applovin_open_app_unit_id, activity);
            maxAppOpenAd.setListener(new MaxAdListener() {
                @Override
                public void onAdLoaded(MaxAd ad) {
                    Log.d(TAG, type + " Open App loaded _ splash");
                    loadTime = (new Date()).getTime();
                    maxAppOpenAd.showAd();
                }

                @Override
                public void onAdDisplayed(MaxAd ad) {
                }

                @Override
                public void onAdHidden(MaxAd ad) {
                    openAppSplashFinish(listener);
                }

                @Override
                public void onAdClicked(MaxAd ad) {
                }

                @Override
                public void onAdLoadFailed(String adUnitId, MaxError error) {
                    Log.d(TAG, type + " Open App load failed _ splash : " + error.getMessage());
                    retryLoadAndShowOpenAppAd(ad_index, retry_count, listener);
                }

                @Override
                public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                    openAppSplashFinish(listener);
                }
            });
            maxAppOpenAd.loadAd();

        } else if (type == APPLOVIN_DISCOVERY && ad_replace_unsupported_open_app_with_interstitial_on_splash) {
            AdRequest.Builder builder = new AdRequest.Builder();
            Bundle interstitialExtras = new Bundle();
            interstitialExtras.putString("zone_id", ad_applovin_interstitial_zone_id);
            builder.addCustomEventExtrasBundle(AppLovinCustomEventInterstitial.class, interstitialExtras);
            AppLovinInterstitialAdDialog appLovinInterstitialAdDialog = AppLovinInterstitialAd.create(AppLovinSdk.getInstance(activity), activity);
            AppLovinSdk.getInstance(activity).getAdService().loadNextAd(AppLovinAdSize.INTERSTITIAL, new AppLovinAdLoadListener() {
                @Override
                public void adReceived(AppLovinAd ad) {
                    Log.d(TAG, type + " Open App loaded _ splash");
                    loadTime = (new Date()).getTime();
                    appLovinInterstitialAdDialog.showAndRender(ad);
                    appLovinInterstitialAdDialog.setAdDisplayListener(new AppLovinAdDisplayListener() {
                        @Override
                        public void adDisplayed(AppLovinAd appLovinAd) {

                        }

                        @Override
                        public void adHidden(AppLovinAd appLovinAd) {
                            openAppSplashFinish(listener);
                        }
                    });
                }

                @Override
                public void failedToReceiveAd(int errorCode) {
                    Log.d(TAG, type + " Open App load failed _ splash : " + errorCode);
                    retryLoadAndShowOpenAppAd(ad_index, retry_count, listener);
                }

            });
        } else {
            openAppSplashFinish(listener);
        }
    }

    public void retryLoadAndShowOpenAppAd(int ad_index, int retry_count, AdOpenListener listener) {
        int adIndex = ad_index + 1;
        int finalRetry = retry_count;
        if (adIndex > ad_networks.length - 1) {
            adIndex = 0;
            finalRetry++;
        }
        final int _adIndex = adIndex, _finalRetry = finalRetry;
        Log.d(TAG, "retryLoadAndShowOpenAppAd ad_index : " + _adIndex + " retry_count : " + _finalRetry);
        new Handler(activity.getMainLooper()).postDelayed(() -> {
            loadAndShowOpenAppAd(_adIndex, _finalRetry, listener);
        }, 500);
    }

    public void openAppSplashFinish(AdOpenListener listener){
        lastShowTime = (new Date()).getTime();
        if (listener != null) {
            listener.onFinish();
        }
    }

    public static void loadOpenAppAd(Context context, int ad_index, int retry_count) {
        if (retry_count > retry_from_start_max) {
            isLoadingAd = false;
            return;
        }

        last_open_app_index = ad_index;
        AdNetworkType type = ad_networks[ad_index];
        isLoadingAd = true;
        if (type == APPLOVIN || type == APPLOVIN_MAX) {
            ad_applovin_appOpenAd = new MaxAppOpenAd(ad_applovin_open_app_unit_id, context);
            ad_applovin_appOpenAd.setListener(new MaxAdListener() {
                @Override
                public void onAdLoaded(MaxAd ad) {
                    Log.d(TAG, type + " Open App loaded");
                    isLoadingAd = false;
                    loadTime = (new Date()).getTime();
                }

                @Override
                public void onAdDisplayed(MaxAd ad) {
                }

                @Override
                public void onAdHidden(MaxAd ad) {

                }

                @Override
                public void onAdClicked(MaxAd ad) {
                }

                @Override
                public void onAdLoadFailed(String adUnitId, MaxError error) {
                    Log.d(TAG, type + " Open App load failed : " + error.getMessage());
                    isLoadingAd = false;
                    retryLoadOpenAppAd(context, ad_index, retry_count);
                }

                @Override
                public void onAdDisplayFailed(MaxAd ad, MaxError error) {

                }
            });
            ad_applovin_appOpenAd.loadAd();

        } else {
            isLoadingAd = false;
        }
    }

    public static void retryLoadOpenAppAd(Context context, int ad_index, int retry_count) {
        int adIndex = ad_index + 1;
        int finalRetry = retry_count;
        if (adIndex > ad_networks.length - 1) {
            adIndex = 0;
            finalRetry++;
        }
        final int _adIndex = adIndex, _finalRetry = finalRetry;
        Log.d(TAG, "retryLoadOpenAppAd ad_index : " + _adIndex + " retry_count : " + _finalRetry);
        new Handler(context.getMainLooper()).postDelayed(() -> {
            loadOpenAppAd(context, _adIndex, _finalRetry);
        }, 500);
    }

    public static void showOpenAppAd(Context context) {
        Log.d(TAG, "showOpenAppAd start");
        if (isLoadingAd) {
            Log.d(TAG, "Open app still loading");
            return;
        }
        if (!wasLoadTimeLessThanNHoursAgo(4)) {
            loadOpenAppAd(context, 0, 0);
            Log.d(TAG, "showOpenAppAd : wasLoadTimeLessThanNHoursAgo");
            return;
        }

        if (!wasShowTimeMoreNSecondAgo(10)) {
            Log.d(TAG, "showOpenAppAd : wasShowTimeMoreNMinuteAgo");
            return;
        }

        if (activityListener == null || ActivityListener.currentActivity == null) {
            Log.d(TAG, "showOpenAppAd : activityListener null");
            return;
        }

        AdNetworkType type = ad_networks[last_open_app_index];
        lastShowTime = (new Date()).getTime();
       if (type == APPLOVIN || type == APPLOVIN_MAX) {
            if (ad_applovin_appOpenAd == null || !ad_applovin_appOpenAd.isReady()) return;
            ad_applovin_appOpenAd.showAd();
        }
        Log.d(TAG, type + " showOpenAppAd");
    }

    // check if ad was loaded more than n hours ago.
    private static boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }

    // check if ad was loaded more than n minute ago.
    private static boolean wasShowTimeMoreNSecondAgo(long second) {
        long difference = (new Date()).getTime() - lastShowTime;
        long numMilliSecondsPerSecond = 1000;
        return (difference > (numMilliSecondsPerSecond * second));
    }
}

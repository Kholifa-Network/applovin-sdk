package kholifa.ads.sdk;

import static kholifa.ads.sdk.AdConfig.ad_admob_open_app_unit_id;
import static kholifa.ads.sdk.AdConfig.ad_enable;
import static kholifa.ads.sdk.AdConfig.ad_enable_banner;
import static kholifa.ads.sdk.AdConfig.ad_enable_interstitial;
import static kholifa.ads.sdk.AdConfig.ad_enable_open_app;
import static kholifa.ads.sdk.AdConfig.ad_enable_rewarded;
import static kholifa.ads.sdk.AdConfig.ad_network;
import static kholifa.ads.sdk.data.AdNetworkType.APPLOVIN;
import static kholifa.ads.sdk.data.AdNetworkType.APPLOVIN_DISCOVERY;
import static kholifa.ads.sdk.data.AdNetworkType.APPLOVIN_MAX;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;

import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.applovin.sdk.AppLovinSdkSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kholifa.ads.sdk.data.AdNetworkType;
import kholifa.ads.sdk.data.SharedPref;
import kholifa.ads.sdk.format.BannerAdFormat;
import kholifa.ads.sdk.format.InterstitialAdFormat;
import kholifa.ads.sdk.format.OpenAppAdFormat;
import kholifa.ads.sdk.format.RewardAdFormat;
import kholifa.ads.sdk.gdpr.UMP;
import kholifa.ads.sdk.helper.AudienceNetworkInitializeHelper;
import kholifa.ads.sdk.listener.AdOpenListener;
import kholifa.ads.sdk.listener.AdRewardedListener;
import kholifa.ads.sdk.utils.Tools;

public class AdNetwork {

    private static final String TAG = AdNetwork.class.getSimpleName();

    private final Activity activity;
    private final SharedPref sharedPref;
    private static BannerAdFormat bannerAdFormat;
    private static InterstitialAdFormat interstitialAdFormat;
    private static RewardAdFormat rewardAdFormat;
    private static OpenAppAdFormat openAppAdFormat;
    public static String GAID = "";

    private static List<AdNetworkType> ad_networks = new ArrayList<>();

    public AdNetwork(Activity activity) {
        this.activity = activity;
        sharedPref = new SharedPref(activity);
        if (ad_enable_banner) bannerAdFormat = new BannerAdFormat(activity);
        if (ad_enable_interstitial) interstitialAdFormat = new InterstitialAdFormat(activity);
        if (ad_enable_rewarded) rewardAdFormat = new RewardAdFormat(activity);
        if (ad_enable_open_app) openAppAdFormat = new OpenAppAdFormat(activity);
        Tools.getGAID(activity);
    }

    public void init() {
        if (!ad_enable) return;

        // check if using single networks
        if (AdConfig.ad_networks.length == 0) {
            AdConfig.ad_networks = new AdNetworkType[]{
                    ad_network
            };
        }

        ad_networks = Arrays.asList(AdConfig.ad_networks);

        // init applovin
        if (Tools.contains(ad_networks, APPLOVIN, APPLOVIN_MAX)) {
            Log.d(TAG, "APPLOVIN, APPLOVIN_MAX, FAN_BIDDING_APPLOVIN_MAX init");
            AppLovinSdk appLovinSdk;
            AppLovinSdkSettings settings = new AppLovinSdkSettings(activity);
            settings.setTestDeviceAdvertisingIds(Arrays.asList(GAID));
            appLovinSdk = AppLovinSdk.getInstance(activity);
            if (BuildConfig.DEBUG) {
                appLovinSdk = AppLovinSdk.getInstance(settings, activity);
                appLovinSdk.showMediationDebugger();
            }
            appLovinSdk.setMediationProvider(AppLovinMediationProvider.MAX);
            appLovinSdk.initializeSdk(new AppLovinSdk.SdkInitializationListener() {
                @Override
                public void onSdkInitialized(AppLovinSdkConfiguration appLovinSdkConfiguration) {
                    Log.d(TAG, "APPLOVIN, APPLOVIN_MAX, FAN_BIDDING_APPLOVIN_MAX onSdkInitialized");
                }
            });
            AudienceNetworkInitializeHelper.initialize(activity);
        }

        // init applovin discovery
        if (Tools.contains(ad_networks, APPLOVIN_DISCOVERY)) {
            Log.d(TAG, "APPLOVIN_DISCOVERY init");
            AppLovinSdk.initializeSdk(activity);
        }

        // save to shared pref
        sharedPref.setOpenAppUnitId(ad_admob_open_app_unit_id);
    }

    public void loadBannerAd(boolean enable, LinearLayout ad_container) {
        if (!ad_enable || bannerAdFormat == null || !enable) return;
        bannerAdFormat.loadBannerAdMain(0, 0, ad_container);
    }

    public void loadInterstitialAd(boolean enable) {
        if (!ad_enable || interstitialAdFormat == null || !enable) return;
        interstitialAdFormat.loadInterstitialAd(0, 0);
    }

    public boolean showInterstitialAd(boolean enable) {
        if (!ad_enable || interstitialAdFormat == null || !enable) return false;
        return interstitialAdFormat.showInterstitialAd();
    }

    public void loadRewardedAd(boolean enable, AdRewardedListener listener) {
        if (!ad_enable || rewardAdFormat == null || !enable) return;
        rewardAdFormat.loadRewardAd(0, 0, listener);
    }

    public boolean showRewardedAd(boolean enable, AdRewardedListener listener) {
        if (!ad_enable || rewardAdFormat == null || !enable) return false;
        return rewardAdFormat.showRewardAd(listener);
    }

    public void loadAndShowOpenAppAd(Activity activity, boolean enable, AdOpenListener listener) {
        if (!ad_enable || openAppAdFormat == null || !enable) {
            if (listener != null) listener.onFinish();
            return;
        }
        openAppAdFormat.loadAndShowOpenAppAd(0, 0, listener);
    }

    public static void loadOpenAppAd(Context context, boolean enable) {
        if (!ad_enable || openAppAdFormat == null || !enable) return;
        OpenAppAdFormat.loadOpenAppAd(context, 0, 0);
    }

    public static void showOpenAppAd(Context context, boolean enable) {
        if (!ad_enable || openAppAdFormat == null || !enable) return;
        OpenAppAdFormat.showOpenAppAd(context);
    }

    public void destroyAndDetachBanner() {
        if (bannerAdFormat == null) return;
        bannerAdFormat.destroyAndDetachBanner(ad_networks);
    }

    public void loadShowUMPConsentForm() {
        new UMP(activity).loadShowConsentForm();
    }

}

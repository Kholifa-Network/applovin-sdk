package kholifa.ads.sdk.format;

import static kholifa.ads.sdk.AdConfig.ad_applovin_banner_unit_id;
import static kholifa.ads.sdk.AdConfig.ad_applovin_banner_zone_id;
import static kholifa.ads.sdk.AdConfig.ad_networks;
import static kholifa.ads.sdk.AdConfig.retry_from_start_max;
import static kholifa.ads.sdk.data.AdNetworkType.APPLOVIN;
import static kholifa.ads.sdk.data.AdNetworkType.APPLOVIN_DISCOVERY;
import static kholifa.ads.sdk.data.AdNetworkType.APPLOVIN_MAX;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.applovin.adview.AppLovinAdView;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinSdkUtils;
import com.google.android.gms.ads.AdRequest;

import java.util.List;

import kholifa.ads.sdk.AdNetwork;
import kholifa.ads.sdk.data.AdNetworkType;
import kholifa.ads.sdk.helper.AppLovinCustomEventBanner;
import kholifa.ads.sdk.utils.Tools;

public class BannerAdFormat {

    private static final String TAG = AdNetwork.class.getSimpleName();

    private final Activity activity;
    private LinearLayout adContainer;

    public BannerAdFormat(Activity activity) {
        this.activity = activity;
    }

    public void loadBannerAdMain(int ad_index, int retry_count, LinearLayout ad_container) {
        if (retry_count > retry_from_start_max) return;

        ad_container.setVisibility(View.GONE);
        ad_container.removeAllViews();
        AdNetworkType type = ad_networks[ad_index];
        ad_container.post(() -> {
            if (type == APPLOVIN || type == APPLOVIN_MAX) {
                Log.d(TAG, type.name() + " loadBannerAdMain");
                MaxAdView maxAdView = new MaxAdView(ad_applovin_banner_unit_id, activity);
                maxAdView.setListener(new MaxAdViewAdListener() {
                    @Override
                    public void onAdExpanded(MaxAd ad) {

                    }

                    @Override
                    public void onAdCollapsed(MaxAd ad) {

                    }

                    @Override
                    public void onAdLoaded(MaxAd ad) {
                        Log.d(TAG, type.name() + " onBannerAdLoaded");
                        ad_container.setVisibility(View.VISIBLE);
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
                        Log.d(TAG, type.name() + " onAdLoadFailed " + error.getMessage());
                        ad_container.setVisibility(View.GONE);
                        retryLoadBanner(ad_index, retry_count, ad_container);
                    }

                    @Override
                    public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                        Log.d(TAG, type.name() + " onAdDisplayFailed " + error.getMessage());
                        ad_container.setVisibility(View.GONE);
                        retryLoadBanner(ad_index, retry_count, ad_container);
                    }
                });

                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int heightPx = Tools.dpToPx(activity, 50);
                maxAdView.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx));
                ad_container.addView(maxAdView);
                maxAdView.loadAd();
            } else if (type == APPLOVIN_DISCOVERY) {
                AdRequest.Builder builder = new AdRequest.Builder();
                Bundle bannerExtras = new Bundle();
                bannerExtras.putString("zone_id", ad_applovin_banner_zone_id);
                builder.addCustomEventExtrasBundle(AppLovinCustomEventBanner.class, bannerExtras);

                boolean isTablet2 = AppLovinSdkUtils.isTablet(activity);
                AppLovinAdSize adSize = isTablet2 ? AppLovinAdSize.LEADER : AppLovinAdSize.BANNER;
                AppLovinAdView adView = new AppLovinAdView(adSize, activity);
                adView.setAdLoadListener(new AppLovinAdLoadListener() {
                    @Override
                    public void adReceived(AppLovinAd ad) {
                        ad_container.setVisibility(View.VISIBLE);
                        Log.d(TAG, type.name() + " adReceived");
                    }

                    @Override
                    public void failedToReceiveAd(int errorCode) {
                        ad_container.setVisibility(View.GONE);
                        Log.d(TAG, type.name() + " failedToReceiveAd : " + errorCode);
                        retryLoadBanner(ad_index, retry_count, ad_container);
                    }
                });
                ad_container.addView(adView);
                adView.loadNextAd();
            }
        });

        adContainer = ad_container;
    }

    private void retryLoadBanner(int ad_index, int retry_count, LinearLayout ad_container) {
        int adIndex = ad_index + 1;
        int finalRetry = retry_count;
        if (adIndex > ad_networks.length - 1) {
            adIndex = 0;
            finalRetry++;
        }
        final int _adIndex = adIndex, _finalRetry = finalRetry;
        Log.d(TAG, "delayAndLoadBanner ad_index : " + _adIndex + " retry_count : " + _finalRetry);
        new Handler(activity.getMainLooper()).postDelayed(() -> {
            loadBannerAdMain(_adIndex, _finalRetry, ad_container);
        }, 3000);
    }


    public void destroyAndDetachBanner(List<AdNetworkType> adNetworks) {

    }

}

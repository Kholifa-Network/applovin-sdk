package kholifa.ads.sdk;

import java.io.Serializable;

import kholifa.ads.sdk.data.AdNetworkType;

public class AdConfig implements Serializable {

    public static boolean ad_enable = true;
    public static boolean ad_enable_banner = true;
    public static boolean ad_enable_interstitial = true;
    public static boolean ad_enable_rewarded = true;
    public static boolean ad_enable_open_app = true;
    public static boolean ad_replace_unsupported_open_app_with_interstitial_on_splash = true;
    public static int limit_time_open_app_loading = 10;
    public static boolean debug_mode = true;
    public static boolean enable_gdpr = true;

    public static AdNetworkType ad_network = AdNetworkType.APPLOVIN;

    public static int retry_from_start_max = 0;

    public static AdNetworkType[] ad_networks = {
            AdNetworkType.APPLOVIN
    };

    public static int ad_inters_interval = 0;

    public static String ad_admob_publisher_id = "pub-3940256099942544";
    public static String ad_admob_banner_unit_id = "ca-app-pub-3940256099942544/6300978111";
    public static String ad_admob_interstitial_unit_id = "ca-app-pub-3940256099942544/1033173712";
    public static String ad_admob_rewarded_unit_id = "ca-app-pub-3940256099942544/5224354917";
    public static String ad_admob_open_app_unit_id = "ca-app-pub-3940256099942544/3419835294";

    public static String ad_manager_banner_unit_id = "/6499/example/banner";
    public static String ad_manager_interstitial_unit_id = "/6499/example/interstitial";
    public static String ad_manager_rewarded_unit_id = "/6499/example/rewarded";
    public static String ad_manager_open_app_unit_id = "/6499/example/app-open";

    public static String ad_fan_banner_unit_id = "YOUR_PLACEMENT_ID";
    public static String ad_fan_interstitial_unit_id = "YOUR_PLACEMENT_ID";
    public static String ad_fan_rewarded_unit_id = "YOUR_PLACEMENT_ID";

    public static String ad_ironsource_app_key = "170112cfd";
    public static String ad_ironsource_banner_unit_id = "DefaultBanner";
    public static String ad_ironsource_rewarded_unit_id = "DefaultRewardedVideo";
    public static String ad_ironsource_interstitial_unit_id = "DefaultInterstitial";

    public static String ad_unity_game_id = "4988568";
    public static String ad_unity_banner_unit_id = "Banner_Android";
    public static String ad_unity_rewarded_unit_id = "Rewarded_Android";
    public static String ad_unity_interstitial_unit_id = "Interstitial_Android";

    public static String ad_applovin_banner_unit_id = "a3a3a5b44c763304";
    public static String ad_applovin_interstitial_unit_id = "35f9c01af124fcb9";
    public static String ad_applovin_rewarded_unit_id = "21dba76a66f7c9fe";
    public static String ad_applovin_open_app_unit_id = "7c3fcecd43d3f90c";

    public static String ad_applovin_banner_zone_id = "df40a31072feccab";
    public static String ad_applovin_interstitial_zone_id = "d0eea040d4bd561e";
    public static String ad_applovin_rewarded_zone_id = "5d799aeefef733a1";

    public static String ad_startapp_app_id = "0";

    public static String ad_wortise_app_id = "test-app-id";
    public static String ad_wortise_banner_unit_id = "test-banner";
    public static String ad_wortise_interstitial_unit_id = "test-interstitial";
    public static String ad_wortise_rewarded_unit_id = "test-rewarded";
    public static String ad_wortise_open_app_unit_id = "test-app-open";

}

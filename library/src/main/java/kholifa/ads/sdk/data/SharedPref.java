package kholifa.ads.sdk.data;

import android.content.Context;
import android.content.SharedPreferences;

import kholifa.ads.sdk.AdConfig;

public class SharedPref {

    private Context ctx;
    private SharedPreferences custom_prefence;

    public SharedPref(Context context) {
        this.ctx = context;
        custom_prefence = context.getSharedPreferences("android-ads-sdk", Context.MODE_PRIVATE);
    }

    // Preference for first launch
    public void setIntersCounter(int counter) {
        custom_prefence.edit().putInt("INTERS_COUNT", counter).apply();
    }

    public int getIntersCounter() {
        return custom_prefence.getInt("INTERS_COUNT", 0);
    }

    public void clearIntersCounter() {
        custom_prefence.edit().putInt("INTERS_COUNT", 0).apply();
    }


    // save open app unit id
    public void setOpenAppUnitId(String value) {
        custom_prefence.edit().putString("OPEN_APP_ID", value).apply();
    }

    public String getOpenAppUnitId() {
        return custom_prefence.getString("OPEN_APP_ID", AdConfig.ad_admob_open_app_unit_id);
    }


}

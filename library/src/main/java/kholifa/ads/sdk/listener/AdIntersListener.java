package kholifa.ads.sdk.listener;

public interface AdIntersListener {
    void onLoaded();
    void onFailed();
    void onDismissed();
    void onShow();
}

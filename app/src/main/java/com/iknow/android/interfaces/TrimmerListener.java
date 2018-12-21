package com.iknow.android.interfaces;


public interface TrimmerListener {
    void onStartTrim();
    void onFinishTrim(String url,boolean addmusic);
    void onCancel();
}

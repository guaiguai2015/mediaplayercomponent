package com.guaiguai.wrl.minecomponent.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.guaiguai.wrl.minecomponent.application.ComponentApplication;

/**
 * 对程序中使用到的sharedpreferenced的使用进行封装
 */

public class SharedPreferencedManager {

    private static SharedPreferencedManager mInstance;

    private static SharedPreferences sp;
    private static SharedPreferences.Editor ed;

    private static final String SHARED_PREFERENCED_NAME = "wrl_pre";

    public static final String VIDEO_PLAY_SETTING = "play_video_setting";

    private SharedPreferencedManager () {
        sp = ComponentApplication.getInstance().getSharedPreferences(SHARED_PREFERENCED_NAME, Context.MODE_PRIVATE);
        ed = sp.edit();
    }

    public static SharedPreferencedManager getInstance () {
        if (mInstance == null || sp == null || ed == null) {
            mInstance = new SharedPreferencedManager();
        }
        return mInstance;
    }

    public void putString (String key,String value) {
        ed.putString(key,value);
        ed.commit();
    }

    public String getString (String key,String defaultValue) {
        return sp.getString(key,defaultValue);
    }

    public void putInt (String key,int value) {
        ed.putInt(key,value);
        ed.commit();
    }

    public int getInt (String key,int defaultValue) {
        return sp.getInt(key,defaultValue);
    }

    public void putLong (String key,Long value) {
        ed.putLong(key,value);
        ed.commit();
    }

    public Long getLong (String key,Long defaultvalue) {
        return sp.getLong(key,defaultvalue);
    }

    public void putBoolean (String key,boolean value) {
        ed.putBoolean(key,value);
        ed.commit();
    }

    public boolean getBoolean (String key,boolean defaultValue) {
        return sp.getBoolean(key,defaultValue);
    }

    public boolean isKeyExist (String key) {
        return sp.contains(key);
    }

    public void remove (String key) {
        ed.remove(key);
        ed.commit();
    }
}

package com.guaiguai.wrl.mylibrary.constant;

/**
 * Created by wei on 2017/5/18.
 */

public class SDKConstant {


    //自动播放阈值
    public static int VIDEO_SCREEN_PERCENT = 50;

    //屏幕宽高的比在9：16中
    public static final float VIDEO_HEIGHT_PERCENT = 9 / 16f;

    //自动播放条件
    public enum AutoPlaySetting {
        AUTO_PLAY_ONLY_WIFI,
        AUTO_PLAY_3G_4G_WIFI,
        AUTO_PLAY_NEVER
    }
}

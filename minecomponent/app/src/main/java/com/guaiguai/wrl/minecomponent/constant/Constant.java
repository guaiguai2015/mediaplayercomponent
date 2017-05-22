package com.guaiguai.wrl.minecomponent.constant;

import android.Manifest;

/**
 * Created by wei on 2017/5/18.
 */

public class Constant {


    //发送给底层的请求码 这个自己定义就行
    public static final int HARDWEAR_CAMERA_CODE = 0x02;
    //检查系统的相机的权限
    public static final String[] HARDWEAR_CAMERA_PERMISSION = new String []{Manifest.permission.CAMERA};
}

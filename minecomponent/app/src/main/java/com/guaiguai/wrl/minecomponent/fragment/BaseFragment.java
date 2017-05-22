package com.guaiguai.wrl.minecomponent.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.guaiguai.wrl.minecomponent.constant.Constant;
import com.guaiguai.wrl.minecomponent.fragment.home.HomeFragment;

/**
 * Created by WRL on 2017/5/3.
 */
public class BaseFragment extends Fragment {

    protected Activity mContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 判断是否有这个权限
     * @param permissons
     * @return  false 表示有这个权限  true 表示没有这个权限
     */
    public boolean hasPermission (String... permissons) {
        for (String permission : permissons) {
            if (ContextCompat.checkSelfPermission(mContext,permission)
                   != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;

    }

    //去请求系统的权限
    public void requestPermission (int code,String... permissions) {
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(permissions,code);
        }
    }


    //请求系统权限的时候返回的回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constant.HARDWEAR_CAMERA_CODE) {
             if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                 doOpenCamera();
             }
        }
    }
     //打开照相机
    public void doOpenCamera () {}
}

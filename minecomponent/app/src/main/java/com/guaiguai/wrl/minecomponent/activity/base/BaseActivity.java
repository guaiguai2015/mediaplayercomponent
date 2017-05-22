package com.guaiguai.wrl.minecomponent.activity.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by WRL on 2017/5/3.
 */
public class BaseActivity extends AppCompatActivity {

    /**
     * 获取输出日志的标志
     */
    public String TAG;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = getComponentName().getClassName();
    }
}

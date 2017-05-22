package com.guaiguai.wrl.mylibrary.okhttp.listener;

/**
 * Created by WRL on 2017/5/6.
 */
public interface DisposeDataListener {

    void onSuccess(Object responseObject);

    void onFailure(Object reasonObject);
}

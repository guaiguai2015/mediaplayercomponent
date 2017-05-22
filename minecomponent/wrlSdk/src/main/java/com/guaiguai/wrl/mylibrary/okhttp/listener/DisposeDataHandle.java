package com.guaiguai.wrl.mylibrary.okhttp.listener;

/**
 * Created by WRL on 2017/5/6.
 */
public class DisposeDataHandle {

    public DisposeDataListener listener = null;
    public Class<?> mClass = null;

     public DisposeDataHandle (DisposeDataListener listener) {
          this.listener = listener;
     }

    public DisposeDataHandle (DisposeDataListener listener,Class<?> clazz) {
        this.listener = listener;
        this.mClass = clazz;
    }
}

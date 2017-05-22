package com.guaiguai.wrl.mylibrary.okhttp.response;

import android.os.Handler;
import android.os.Looper;

import com.guaiguai.wrl.mylibrary.adutil.ResponseEntityToModule;
import com.guaiguai.wrl.mylibrary.okhttp.exception.OkHttpException;
import com.guaiguai.wrl.mylibrary.okhttp.listener.DisposeDataHandle;
import com.guaiguai.wrl.mylibrary.okhttp.listener.DisposeDataListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by WRL on 2017/5/6.
 */
public class CommonJsonCallBack implements Callback {

    //需要定义一些变量
    private final String RESULT_CODE = "ecode";  //这个是根据你跟服务器之间商定好的协议
    private final int RESULT_CODE_VALUE = 0;

    //自定义一些异常
    private final int NETWORK_ERROR = -1;
    private final int JSON_ERRON = -2;
    private final int OTHHER_ERROR = -3;
    private final String EMPTY_MSG = "";

    private Handler mDiveryHandler;
    private DisposeDataListener mListener;
    private Class<?> mClass;

    public CommonJsonCallBack (DisposeDataHandle handle) {
        this.mDiveryHandler =  new Handler(Looper.getMainLooper());
        this.mListener = handle.listener;
        this.mClass = handle.mClass;
    }

    @Override
    public void onFailure(Call call, final IOException e) {

        /**
         * 由于现在还在子线程中，要向主线程中发消息
         */
        mDiveryHandler.post(new Runnable() {
            @Override
            public void run() {
                mListener.onFailure(new OkHttpException(NETWORK_ERROR,e));
            }
        });
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {

        final String result = response.body().string();
        mDiveryHandler.post(new Runnable() {
            @Override
            public void run() {
                handleResponse(result);

            }
        });
    }

    private void handleResponse(Object responseObject) {
        if (responseObject == null || responseObject.toString().trim().equals("")) {
            mListener.onFailure(new OkHttpException(NETWORK_ERROR,EMPTY_MSG));
            return;
        }
        try {
            JSONObject result = new JSONObject(responseObject.toString());
            if (result.has(RESULT_CODE)) {

                if (result.getInt(RESULT_CODE) == RESULT_CODE_VALUE) {  //返回的结果码跟你们规定的不一样就是错误的得进行修改
                    if (mClass == null) {
                        mListener.onSuccess(responseObject);
                    }else {
                        //进行解析数据
                        Object obj = ResponseEntityToModule.parseJsonObjectToModule(result, mClass);
                        if (obj == null) {
                            mListener.onFailure(new OkHttpException(JSON_ERRON,EMPTY_MSG));
                        }else {
                            mListener.onSuccess(obj);
                        }
                    }
                }else {
                    mListener.onFailure(new OkHttpException(OTHHER_ERROR,result.get(RESULT_CODE)));
                }
            }else {
                mListener.onFailure(new OkHttpException(OTHHER_ERROR,result.getString(RESULT_CODE)));
            }
        } catch (JSONException e) {
            mListener.onFailure(new OkHttpException(OTHHER_ERROR,e.getMessage()));
            e.printStackTrace();
        }

    }
}

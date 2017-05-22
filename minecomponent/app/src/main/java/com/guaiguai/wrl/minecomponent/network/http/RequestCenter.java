package com.guaiguai.wrl.minecomponent.network.http;

import com.guaiguai.wrl.minecomponent.moudle.recommand.BaseRecommandModel;
import com.guaiguai.wrl.mylibrary.okhttp.CommonOkHttpClient;
import com.guaiguai.wrl.mylibrary.okhttp.listener.DisposeDataHandle;
import com.guaiguai.wrl.mylibrary.okhttp.listener.DisposeDataListener;
import com.guaiguai.wrl.mylibrary.okhttp.request.CommonRequest;
import com.guaiguai.wrl.mylibrary.okhttp.request.RequestParams;

/**
 * Created by Administrator on 2017/5/15.
 */
public class RequestCenter {

    public static void postRequest (String url, RequestParams params, DisposeDataListener listener,Class<?> clazz) {
        CommonOkHttpClient.post(CommonRequest.createPostRequest(url,params),
                new DisposeDataHandle(listener,clazz));
    }

    /**
     * 请求首页的数据
     */

    public static void requestCommandData (DisposeDataListener listener) {
        postRequest(HttpConstants.HOME_RECOMMAND,null,listener, BaseRecommandModel.class);
    }


}

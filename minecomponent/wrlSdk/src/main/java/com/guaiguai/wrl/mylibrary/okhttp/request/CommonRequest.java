package com.guaiguai.wrl.mylibrary.okhttp.request;

import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Request;

/**
 * Created by WRL on 2017/5/6.
 * fuction:一个工具类用来创建okhttp需要传递的request对象
 */
public class CommonRequest {


    /**
     * 用来创建post请求需要的Request
     * @param url
     * @param params
     */
    public static Request createPostRequest (String url, RequestParams params) {

        FormBody.Builder mFormBodyBuilder = new FormBody.Builder();
        if (params != null) {
            for (Map.Entry<String,String> entry : params.urlParams.entrySet()) {
                mFormBodyBuilder.add(entry.getKey(),entry.getValue());
            }
        }

        FormBody formBody = mFormBodyBuilder.build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        return request;
    }

    /**
     * 用来创建get请求需要的Request
     * @param url
     * @param params
     * @return
     */
    public static Request createGetRequest (String url,RequestParams params) {
       StringBuilder builder = new StringBuilder().append(url);
       if (params != null) {
           for (Map.Entry<String, String> entry : params.urlParams.entrySet()) {
               builder.append("?")
                       .append(entry.getKey()).append("=")
                       .append(entry.getValue()).append("&");
           }
       }

       Request request = new Request.Builder()
               .url(builder.substring(0,builder.length() - 1))
               .get()
               .build();
       return request;
   }


    /**
     * @param url
     * @param params
     * @return
     */
    public static Request createMonitorRequest(String url, RequestParams params) {
        StringBuilder urlBuilder = new StringBuilder(url).append("&");
        if (params != null && params.hasParams()) {
            for (Map.Entry<String, String> entry : params.urlParams.entrySet()) {
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        return new Request.Builder().url(urlBuilder.substring(0, urlBuilder.length() - 1)).get().build();
    }
}

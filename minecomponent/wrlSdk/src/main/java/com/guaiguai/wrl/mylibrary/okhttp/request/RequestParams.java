package com.guaiguai.wrl.mylibrary.okhttp.request;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by WRL on 2017/5/6.
 * fuction:用来存储一些参数
 */
public class RequestParams {

    public ConcurrentHashMap<String,String> urlParams = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String,Object> fileParams = new ConcurrentHashMap<>();

    public RequestParams () {
        this((Map<String, String>) null);
    }

    public RequestParams (final String key, final String value) {
        this(new HashMap<String, String>() {
            {
                put(key, value);
            }
        });
    }

    public RequestParams (Map<String,String> sourse) {
        if (sourse != null) {
            for (Map.Entry<String,String> entry: sourse.entrySet()) {
                put(entry.getKey(),entry.getValue());
            }
        }
    }

    public void put (String key,String value) {
        if (key != null && value != null) {
            urlParams.put(key,value);
        }
    }


    public void put(String key,Object object) {
         if (key != null) {
             fileParams.put(key,object);
         }
    }

    public boolean hasParams () {
        if (urlParams.size() > 0 || fileParams.size() > 0) {
            return true;
        }
        return false;
    }
}

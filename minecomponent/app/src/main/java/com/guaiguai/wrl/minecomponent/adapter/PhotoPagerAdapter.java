package com.guaiguai.wrl.minecomponent.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.guaiguai.wrl.minecomponent.util.ImageLoaderManager;
import com.guaiguai.wrl.mylibrary.adutil.Utils;

import java.util.ArrayList;

/**
 * Created by wei on 2017/5/17.
 */

public class PhotoPagerAdapter extends PagerAdapter {

    private ArrayList<String> mData;
    private Context mContext;
    private ArrayList<ImageView> mViews;
    private ImageLoaderManager mImageLoaderManager;



    public PhotoPagerAdapter (Context context,ArrayList<String> data) {

        this.mData = data;
        this.mContext = context;

        mViews = new ArrayList<>();
        mImageLoaderManager = ImageLoaderManager.getInstance(mContext);

        initData();
    }

    private void initData() {
        for (int i = 0;i < mData.size();i++) {
            ImageView imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            LinearLayout.LayoutParams params = new LinearLayout.
                    LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    Utils.dip2px(mContext, 150));
            imageView.setLayoutParams(params);
            mImageLoaderManager.displayImage(imageView,mData.get(i));
            mViews.add(imageView);
        }
    }


    @Override
    public int getCount() {
        return Integer.MAX_VALUE;

    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        position = position % mData.size();

        ImageView view = mViews.get(position);
        //如果View已经在之前添加到了一个父组件，则必须先remove，否则会抛出IllegalStateException。
        ViewParent vp =view.getParent();
        if (vp!=null){
            ViewGroup parent = (ViewGroup)vp;
            parent.removeView(view);
        }


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //在这里面做相应的页面跳转操作
            }
        });

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    }
}

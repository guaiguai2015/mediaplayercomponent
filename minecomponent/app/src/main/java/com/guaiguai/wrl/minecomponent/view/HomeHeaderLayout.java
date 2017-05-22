package com.guaiguai.wrl.minecomponent.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.guaiguai.wrl.minecomponent.R;
import com.guaiguai.wrl.minecomponent.adapter.PhotoPagerAdapter;
import com.guaiguai.wrl.minecomponent.moudle.recommand.RecommandHeadValue;
import com.guaiguai.wrl.minecomponent.util.ImageLoaderManager;
import com.guaiguai.wrl.minecomponent.widget.ViewPagerScroller;

import java.lang.reflect.Field;
import java.util.ArrayList;


/**
 * @author: vision
 * @function:
 * @date: 16/9/2
 */
public class HomeHeaderLayout extends RelativeLayout {

    private static final int MSG_UPDATE_IMAGE = 1; //开始进行轮播
    private static final int MSG_STOP_UPDATE = 2;   //暂停轮播
    private static final int MSG_DELAY = 3000; //轮播间隔时间

    private int currentItem = 0;

    private ImageLoaderManager mImageLoaderManager;
    private Context mContext;
    private RecommandHeadValue mHeadValue;
    private LayoutInflater inflater;

    private ViewPager mViewPager;
    private LinearLayout bannerLinear;

    private PhotoPagerAdapter mAdapter;

    private ArrayList<ImageView> dotsData;    //小圆点的数据源

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what) {
                case MSG_UPDATE_IMAGE:
                    currentItem++;
                    mViewPager.setCurrentItem(currentItem,true);
                    break;

                case MSG_STOP_UPDATE:
                    if (handler.hasMessages(MSG_UPDATE_IMAGE)) {
                          handler.removeMessages(MSG_UPDATE_IMAGE);
                    }
                    break;
            }
        }
    };

    public HomeHeaderLayout(Context context, RecommandHeadValue headValue) {
        this(context,null,headValue);
    }

    public HomeHeaderLayout(Context context, AttributeSet attrs,RecommandHeadValue headValue) {

        super(context, attrs);
        this.mContext = context;
        this.mHeadValue = headValue;

        mImageLoaderManager = ImageLoaderManager.getInstance(mContext);
        inflater = LayoutInflater.from(mContext);
        initView();

    }

    private void initView() {
        View view = inflater.inflate(R.layout.listview_home_head_layout,this);
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        bannerLinear = (LinearLayout) view.findViewById(R.id.linear);

        initDots();
        initViewPagerValue();
        handler.sendEmptyMessageDelayed(MSG_UPDATE_IMAGE,MSG_DELAY);

    }

    private void initViewPagerValue() {
        setViewPagerScrollSpeed();

        mAdapter = new PhotoPagerAdapter(mContext,mHeadValue.ads);
        mViewPager.setAdapter(mAdapter);


        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentItem = position;
                setDotsVisible(currentItem % dotsData.size());

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {
                    case ViewPager.SCROLL_STATE_DRAGGING:   //在拖动的过程中
                        handler.sendEmptyMessage(MSG_STOP_UPDATE);
                        break;
                    case ViewPager.SCROLL_STATE_IDLE:    //在没有拖动页面的时候
                        handler.sendEmptyMessageDelayed(MSG_UPDATE_IMAGE,MSG_DELAY);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * 初始化小圆点
     */
    private void initDots() {
        dotsData =  new ArrayList<>();
        for (int i = 0;i < mHeadValue.ads.size();i++) {
            ImageView imageView = new ImageView(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setLayoutParams(params);
            bannerLinear.addView(imageView);
            dotsData.add(imageView);
        }

        setDotsVisible(0);

    }

    /**
     * 根据需求使得小圆点可见可不见
     * @param position
     */
    private void setDotsVisible(int position) {
        for (int i = 0;i < dotsData.size();i++) {
            if (i == position) {
                dotsData.get(position).setImageResource(R.drawable.bg_message_zan);
            }else {
                dotsData.get(i).setImageResource(R.drawable.arrow_right);
            }
        }

    }

    private void setViewPagerScrollSpeed() {

        try {
            Field mScrooler =  ViewPager.class.getDeclaredField("mScroller");
            mScrooler.setAccessible(true);
            ViewPagerScroller scroller = new ViewPagerScroller(mContext);
            scroller.setScrollDuration(1000);
            mScrooler.set(mViewPager,scroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

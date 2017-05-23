package com.guaiguai.wrl.minecomponent.fragment.home;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.guaiguai.wrl.minecomponent.R;
import com.guaiguai.wrl.minecomponent.adapter.CourseAdapter;
import com.guaiguai.wrl.minecomponent.constant.Constant;
import com.guaiguai.wrl.minecomponent.fragment.BaseFragment;
import com.guaiguai.wrl.minecomponent.moudle.recommand.BaseRecommandModel;
import com.guaiguai.wrl.minecomponent.moudle.recommand.RecommandBodyValue;
import com.guaiguai.wrl.minecomponent.network.http.RequestCenter;
import com.guaiguai.wrl.minecomponent.util.ImageLoaderManager;
import com.guaiguai.wrl.minecomponent.view.HomeHeaderLayout;
import com.guaiguai.wrl.minecomponent.zxing.app.CaptureActivity;
import com.guaiguai.wrl.mylibrary.okhttp.CommonOkHttpClient;
import com.guaiguai.wrl.mylibrary.okhttp.listener.DisposeDataHandle;
import com.guaiguai.wrl.mylibrary.okhttp.listener.DisposeDataListener;
import com.guaiguai.wrl.mylibrary.okhttp.request.CommonRequest;
import com.guaiguai.wrl.mylibrary.okhttp.response.CommonJsonCallBack;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.util.ArrayList;

/**
 * Created by WRL on 2017/5/3.
 */
public class HomeFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final int REQUEST_QRCODE = 0x01;

    /**
     * UI
     */
    private View mContentView;
    private ListView mListView;
    private TextView mQRCodeView;
    private TextView mCategoryView;
    private TextView mSearchView;
    private ImageView mLoadingView;

    /**
     * data
     */
    private CourseAdapter mAdapter;
    private BaseRecommandModel mRecommandData;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestRecommandData();
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mContext = getActivity();
        mContentView = inflater.inflate(R.layout.fragment_home_layout,null);
        initView();
        return mContentView;
    }

    private void initView() {
        mQRCodeView = (TextView) mContentView.findViewById(R.id.qrcode_view);
        mQRCodeView.setOnClickListener(this);
        mCategoryView = (TextView) mContentView.findViewById(R.id.category_view);
        mCategoryView.setOnClickListener(this);
        mSearchView = (TextView) mContentView.findViewById(R.id.search_view);
        mSearchView.setOnClickListener(this);
        mListView = (ListView) mContentView.findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);
        mLoadingView = (ImageView) mContentView.findViewById(R.id.loading_view);
        //加载数据的动画的开启
        AnimationDrawable anim = (AnimationDrawable) mLoadingView.getDrawable();
        anim.start();
    }


    private void requestRecommandData() {
        RequestCenter.requestCommandData(new DisposeDataListener() {

            @Override
            public void onSuccess(Object responseObject) {
                mRecommandData = (BaseRecommandModel) responseObject;
                //更新UI
                showSuccessView();
            }

            @Override
            public void onFailure(Object reasonObject) {
                //显示请求失败View
                showErrorView();
            }

        });
    }

    private void showErrorView() {
        Toast.makeText(mContext,"访问失败，请重新访问",Toast.LENGTH_LONG).show();

    }


    private void showSuccessView() {
        //保证代码的健壮性，在一次的进行判定，数据是否是完整的
        if (mRecommandData.data.list != null && mRecommandData.data.list.size() > 0) {
            mLoadingView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);

            mListView.addHeaderView(new HomeHeaderLayout(mContext, mRecommandData.data.head));
            mAdapter = new CourseAdapter(mContext,mRecommandData.data.list);
            mListView.setAdapter(mAdapter);

            //这个方法是让listview进行滑动的过程中不进行加载图片
            mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(),true,true));

            mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    mAdapter.updateAdInScrollView();
                }
            });
        }else {
            showErrorView();
        }



    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.qrcode_view:
                //首先判断是否有相机的权限
                if (hasPermission(Constant.HARDWEAR_CAMERA_PERMISSION)) {
                    doOpenCamera();
                }else {
                    requestPermission(Constant.HARDWEAR_CAMERA_CODE,
                            Constant.HARDWEAR_CAMERA_PERMISSION);
                }
                break;
        }

    }

    @Override
    public void doOpenCamera() {
        Intent intent = new Intent(mContext, CaptureActivity.class);
        startActivityForResult(intent,REQUEST_QRCODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_QRCODE) {    //扫描之后返回的信息
            if (resultCode == Activity.RESULT_OK) {
                String code = data.getStringExtra("SCAN_RESULT");
                if (code.contains("http") || code.contains("https")) {

                }else {
                    Toast.makeText(mContext,"扫描成功",Toast.LENGTH_LONG ).show();
                }

            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}

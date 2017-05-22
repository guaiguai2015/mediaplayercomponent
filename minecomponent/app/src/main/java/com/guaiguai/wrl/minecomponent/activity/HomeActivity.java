package com.guaiguai.wrl.minecomponent.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guaiguai.wrl.minecomponent.R;
import com.guaiguai.wrl.minecomponent.activity.base.BaseActivity;
import com.guaiguai.wrl.minecomponent.fragment.home.HomeFragment;
import com.guaiguai.wrl.minecomponent.fragment.home.MessageFragment;
import com.guaiguai.wrl.minecomponent.fragment.home.MineFragment;

/**
 * Created by WRL on 2017/5/3.
 */
public class HomeActivity extends BaseActivity implements View.OnClickListener {

    private FragmentManager fm;
    private HomeFragment mHomeFragment;
    private Fragment mCommonFragmentOne;
    private MessageFragment mMessageFragment;
    private MineFragment mMineFragment;
    private Fragment mCurrent;

    private RelativeLayout mHomeLayout;
    private RelativeLayout mPondLayout;
    private RelativeLayout mMessageLayout;
    private RelativeLayout mMineLayout;

    private TextView mHomeView;
    private TextView mPondView;
    private TextView mMessageView;
    private TextView mMineView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_layout);
        initView();

        mHomeFragment =  new HomeFragment();
        fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.content_layout,mHomeFragment);
        fragmentTransaction.commit();

    }

    private void initView() {
        mHomeLayout = (RelativeLayout) findViewById(R.id.home_layout_view);
        mHomeLayout.setOnClickListener(this);
        mPondLayout = (RelativeLayout) findViewById(R.id.pond_layout_view);
        mPondLayout.setOnClickListener(this);
        mMessageLayout = (RelativeLayout) findViewById(R.id.message_layout_view);
        mMessageLayout.setOnClickListener(this);
        mMineLayout = (RelativeLayout) findViewById(R.id.mine_layout_view);
        mMineLayout.setOnClickListener(this);

        mHomeView = (TextView) findViewById(R.id.home_image_view);
        mPondView = (TextView) findViewById(R.id.fish_image_view);
        mMessageView = (TextView) findViewById(R.id.message_image_view);
        mMineView = (TextView) findViewById(R.id.mine_image_view);
        mHomeView.setBackgroundResource(R.drawable.comui_tab_home_selected);
    }

    /**
     * 用于隐藏fragment
     * @param fragment
     * @param ft
     */
    private void hideFragment (Fragment fragment,FragmentTransaction ft) {

        if (fragment != null) {
            ft.hide(fragment);
        }
    }

    @Override
    public void onClick(View v) {
        FragmentTransaction ft = fm.beginTransaction();

        switch (v.getId()) {
            case R.id.home_layout_view:
                mHomeView.setBackgroundResource(R.drawable.comui_tab_home_selected);
                mMessageView.setBackgroundResource(R.drawable.comui_tab_message);
                mMineView.setBackgroundResource(R.drawable.comui_tab_person);

                hideFragment(mMessageFragment,ft);
                hideFragment(mMineFragment,ft);
                if (mHomeFragment == null) {
                    mHomeFragment = new HomeFragment();
                    ft.add(R.id.content_layout,mHomeFragment);
                }else {
                    ft.show(mHomeFragment);
                }
                break;
            case R.id.message_layout_view:
                mMessageView.setBackgroundResource(R.drawable.comui_tab_message_selected);
                mHomeView.setBackgroundResource(R.drawable.comui_tab_home);
                mMineView.setBackgroundResource(R.drawable.comui_tab_person);

                hideFragment(mHomeFragment,ft);
                hideFragment(mMineFragment,ft);
                if (mMessageFragment == null) {
                    mMessageFragment = new MessageFragment();
                    ft.add(R.id.content_layout,mMessageFragment);
                }else {
                    ft.show(mMessageFragment);
                }
                break;
            case R.id.mine_layout_view:
                mMineView.setBackgroundResource(R.drawable.comui_tab_person_selected);
                mHomeView.setBackgroundResource(R.drawable.comui_tab_home);
                mMessageView.setBackgroundResource(R.drawable.comui_tab_message);

                hideFragment(mHomeFragment,ft);
                hideFragment(mMessageFragment,ft);
                if (mMineFragment == null) {
                    mMineFragment = new MineFragment();
                    ft.add(R.id.content_layout,mMineFragment);
                }else {
                    ft.show(mMineFragment);
                }
                break;
        }

        ft.commit();

    }
}

package com.guaiguai.wrl.minecomponent.fragment.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.guaiguai.wrl.minecomponent.R;
import com.guaiguai.wrl.minecomponent.fragment.BaseFragment;

/**
 * Created by WRL on 2017/5/3.
 */
public class MineFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mContext = getActivity();
        View view = inflater.inflate(R.layout.fragment_mine_layout,null);

        return view;
    }
}

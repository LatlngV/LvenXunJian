package cn.eyesw.lvenxunjian.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseFragment extends Fragment {

    private Unbinder mUnbinder;
    public Context mContext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getContentLayoutRes(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);
        mContext = getActivity().getApplicationContext();

        initView();
    }

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        mUnbinder = null;
        super.onDestroyView();
    }

    /**
     * 子类实现视图绑定
     */
    public abstract int getContentLayoutRes();

    /**
     * 子类实现初始化视图的方法
     */
    protected abstract void initView();

}

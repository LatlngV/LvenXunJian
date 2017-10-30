package cn.eyesw.lvenxunjian.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Activity 的基类
 */
public abstract class BaseActivity extends AppCompatActivity {

    private Unbinder mUnbinder;
    public Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentLayoutRes());
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mUnbinder = ButterKnife.bind(this);
        mContext = this.getApplicationContext();

        // 初始化 Toolbar
        initToolbar();

        // 初始化相关视图
        initView();
    }

    /**
     * 跳转到另一个 Class(Activity)
     */
    public void startActivity(Class clz) {
        Intent intent = new Intent(mContext, clz);
        startActivity(intent);
    }

    /**
     * 携带参数跳转到另一个 Class(Activity)
     */
    public void startActivity(Class clz, Bundle bundle) {
        Intent intent = new Intent(mContext, clz);
        intent.putExtra("bundle", bundle);
        startActivity(intent);
    }

    /**
     * 吐司通知
     */
    public void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        mUnbinder.unbind();
        mUnbinder = null;
        super.onDestroy();
    }

    /**
     * 子类实现视图
     */
    @LayoutRes
    protected abstract int getContentLayoutRes();

    /**
     * 初始化 Toolbar
     */
    protected abstract void initToolbar();

    /**
     * 初始化相关视图
     */
    protected abstract void initView();

}

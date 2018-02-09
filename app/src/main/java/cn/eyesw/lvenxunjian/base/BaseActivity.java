package cn.eyesw.lvenxunjian.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.eyesw.lvenxunjian.R;
import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;

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
     * 吐司通知
     */
    public void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 设置 Fragment
     */
    public void replaceFragment(int id, Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(id, fragment)
                .commit();
    }

    /**
     * 6.0 授权
     */
    public void permission(List<PermissionItem> list, OnPermissionCallback onPermissionCallback) {
        HiPermission.create(this)
                .title("授权")
                .permissions(list)
                .animStyle(R.style.PermissionAnimModal)
                .filterColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme()))
                .msg("开启权限")
                .checkMutiPermission(new PermissionCallback() {
                    @Override
                    public void onClose() {
                        showToast("授权关闭");
                    }

                    @Override
                    public void onFinish() {
                        // API 23 以下走此方法
                        onPermissionCallback.onPermissionCallback();
                    }

                    @Override
                    public void onDeny(String permission, int position) {
                    }

                    @Override
                    public void onGuarantee(String permission, int position) {
                        // API 23 以上（包括 23）走此方法
                        onPermissionCallback.onPermissionCallback();
                    }
                });
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

    /**
     * 权限回调接口
     */
    public interface OnPermissionCallback {
        void onPermissionCallback();
    }

}

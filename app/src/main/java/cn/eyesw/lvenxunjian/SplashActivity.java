package cn.eyesw.lvenxunjian;

import android.content.Intent;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;

import butterknife.BindView;
import cn.eyesw.lvenxunjian.base.BaseActivity;
import cn.eyesw.lvenxunjian.utils.SpUtil;

/**
 * 闪屏界面
 */
public class SplashActivity extends BaseActivity {

    private SpUtil mSpUtil;

    @BindView(R.id.activity_splash)
    protected RelativeLayout mRelativeLayout;

    @Override
    protected int getContentLayoutRes() {
        // 加载布局之前先移除状态栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        return R.layout.activity_splash;
    }

    @Override
    protected void initToolbar() {
    }

    @Override
    protected void initView() {
        mSpUtil = SpUtil.getInstance(mContext);

        // 透明度动画
        AlphaAnimation alpha = new AlphaAnimation(0.3f, 1f);
        alpha.setDuration(1500);
        alpha.setFillAfter(true);

        // 缩放动画
        ScaleAnimation scale = new ScaleAnimation(0f, 1f, 0f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(1500);
        scale.setFillAfter(true);

        // 将缩放动画和透明动画添加到动画集合里
        AnimationSet set = new AnimationSet(false);
        set.addAnimation(alpha);
        set.addAnimation(scale);

        // 设置动画监听
        set.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 跳转到另一个页面
                jumpToNextPage();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // 开启动画
        mRelativeLayout.startAnimation(set);
    }

    /**
     * 跳转到另一个页面
     */
    private void jumpToNextPage() {
        // 如果之前登陆过就跳转到主界面，否则就跳转到登陆界面
        if (mSpUtil.getBoolean("isLogin")) {
            Intent intent = new Intent(mContext, MainActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        }
        // 结束当前界面
        finish();
    }

}

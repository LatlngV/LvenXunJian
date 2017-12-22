package cn.eyesw.lvenxunjian;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import cn.eyesw.lvenxunjian.base.BaseActivity;
import cn.eyesw.lvenxunjian.constant.Constant;
import cn.eyesw.lvenxunjian.constant.NetworkApi;
import cn.eyesw.lvenxunjian.utils.OkHttpManager;
import cn.eyesw.lvenxunjian.utils.SpUtil;
import okhttp3.Call;

/**
 * 闪屏界面
 * <p>
 * 程序入口
 * </p>
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

        // 检查版本更新
        checkVersion();

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

    private void checkVersion() {
        Map<String, String> map = new HashMap<>();
        map.put("staff_id", mSpUtil.getString("id"));
        OkHttpManager.getInstance()
                .postAsyncForm(NetworkApi.VERSION, map, new OkHttpManager.DataCallback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        showToast("网络连接失败");
                    }

                    @Override
                    public void onResponse(String json) {
                        try {
                            JSONObject object = new JSONObject(json);
                            int code = object.getInt("code");
                            if (code == 200) {
                                JSONObject data = object.getJSONObject("data");
                                String version = data.getString("app_version");
                                String appUrl = data.getString("app_download_url");

                                if (!version.equals(getVersion())) {
                                    mSpUtil.putBoolean(Constant.VERSION_UPDATE, true);
                                    mSpUtil.putString(Constant.APK_URL, appUrl);
                                } else {
                                    mSpUtil.putBoolean(Constant.VERSION_UPDATE, false);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 获取当前版本号
     */
    private String getVersion() throws Exception {
        // 获取 PackageManager 的实例
        PackageManager packageManager = getPackageManager();
        // getPackageName() 是你当前类的包名，0 代表是获取版本信息
        PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
        return packInfo.versionName;
    }

    /**
     * 跳转到另一个页面
     */
    private void jumpToNextPage() {
        // 如果之前登陆过就跳转到主界面，否则就跳转到登陆界面
        if (mSpUtil.getBoolean("isLogin")) {
            if (mSpUtil.getString("roleName").equals("巡检人员")) {
                Intent intent = new Intent(mContext, MainActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(mContext, HomeActivity.class);
                startActivity(intent);
            }
        } else {
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        }
        // 结束当前界面
        finish();
    }

}

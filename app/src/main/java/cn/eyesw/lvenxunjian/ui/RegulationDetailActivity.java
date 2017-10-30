package cn.eyesw.lvenxunjian.ui;

import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import cn.eyesw.lvenxunjian.R;
import cn.eyesw.lvenxunjian.base.BaseActivity;
import cn.eyesw.lvenxunjian.constant.NetworkApi;
import cn.eyesw.lvenxunjian.utils.ToolbarUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 法律详情
 */
public class RegulationDetailActivity extends BaseActivity {

    @BindView(R.id.rd_toolbar)
    protected Toolbar mToolbar;
    @BindView(R.id.rd_tv_title)
    protected TextView mTvTitle;
    @BindView(R.id.rd_tv_detail)
    protected TextView mTvDetail;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.activity_regulation_detail;
    }

    @Override
    protected void initToolbar() {
        ToolbarUtil toolbarUtil = new ToolbarUtil(this);
        toolbarUtil.setToolbar(mToolbar, "详情");
    }

    @Override
    protected void initView() {
        OkHttpClient client = new OkHttpClient();
        FormBody formBody = new FormBody.Builder()
                .add("staff_id", "id")
                .add("rule_id", getIntent().getStringExtra("rule_id"))
                .build();
        Request request = new Request.Builder()
                .url(NetworkApi.RULE_DETAIL)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(mContext, "网络连接失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                analysisData(json);
            }
        });
    }

    private void analysisData(String json) {
        try {
            JSONObject object = new JSONObject(json);
            int code = object.getInt("code");
            if (code == 200) {
                JSONObject data = object.getJSONObject("data").getJSONObject("rule");
                String title = data.getString("title");
                String content = data.getString("content");

                runOnUiThread(() -> {
                    mTvTitle.setText(title);
                    mTvDetail.setText(content);
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}

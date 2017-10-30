package cn.eyesw.lvenxunjian.ui;

import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import cn.eyesw.lvenxunjian.R;
import cn.eyesw.lvenxunjian.base.BaseActivity;
import cn.eyesw.lvenxunjian.constant.NetworkApi;
import cn.eyesw.lvenxunjian.utils.OkHttpManager;
import cn.eyesw.lvenxunjian.utils.SpUtil;
import cn.eyesw.lvenxunjian.utils.ToolbarUtil;
import okhttp3.Call;

/**
 * 我的消息
 */
public class MyMessageActivity extends BaseActivity {

    private List<Map<String, String>> mData;

    @BindView(R.id.message_toolbar)
    protected Toolbar mToolbar;
    @BindView(R.id.message_list_view)
    protected ListView mListView;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.activity_my_message;
    }

    @Override
    protected void initToolbar() {
        ToolbarUtil toolbarUtil = new ToolbarUtil(this);
        toolbarUtil.setToolbar(mToolbar, "我的消息");
    }

    @Override
    protected void initView() {
        mData = new ArrayList<>();

        Map<String, String> map = new HashMap<>();
        map.put("staff_id", SpUtil.getInstance(mContext).getString("id"));
        OkHttpManager.getInstance().postAsyncForm(NetworkApi.MY_MESSAGE, map, new OkHttpManager.DataCallback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(mContext, "网络连接失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String json) {
                try {
                    JSONObject object = new JSONObject(json);
                    int code = object.getInt("code");
                    if (code == 200) {
                        JSONArray array = object.getJSONArray("data");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject data = (JSONObject) array.get(i);
                            String messageTitle = data.getString("message_title");
                            String messageContent = data.getString("message_content");
                            String createtime = data.getString("createtime");

                            Map<String, String> hashMap = new HashMap<>();
                            hashMap.put("title", messageTitle);
                            hashMap.put("content", messageContent);
                            hashMap.put("createtime", createtime);
                            mData.add(hashMap);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mListView.setAdapter(new MyMessageAdapter());
            }
        });
    }

    private class MyMessageAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mData != null ? mData.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.message_list_view, null);
            }
            TextView tvTitle = (TextView) convertView.findViewById(R.id.item_tv_title);
            TextView tvCreateTime = (TextView) convertView.findViewById(R.id.item_tv_create_time);
            TextView tvContent = (TextView) convertView.findViewById(R.id.item_tv_content);

            Map<String, String> map = mData.get(position);
            tvTitle.setText(map.get("title"));
            tvCreateTime.setText(map.get("createtime"));
            tvContent.setText(map.get("content"));
            return convertView;
        }
    }

}

package cn.eyesw.lvenxunjian.fragment;

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
import cn.eyesw.lvenxunjian.base.BaseFragment;
import cn.eyesw.lvenxunjian.constant.NetworkApi;
import cn.eyesw.lvenxunjian.utils.OkHttpManager;
import cn.eyesw.lvenxunjian.utils.SpUtil;
import okhttp3.Call;

/**
 * 月度巡检记录
 */
public class MonthRecordFragment extends BaseFragment {

    private List<Map<String, String>> mDatas;

    @BindView(R.id.month_record_list_view)
    protected ListView mListView;
    @BindView(R.id.month_record_tv_empty)
    protected TextView mTvEmpty;

    @Override
    public int getContentLayoutRes() {
        return R.layout.fragment_month_record;
    }

    @Override
    protected void initView() {
        mDatas = new ArrayList<>();

        Map<String, String> map = new HashMap<>();
        map.put("staff_id", SpUtil.getInstance(mContext).getString("id"));
        OkHttpManager.getInstance().postAsyncForm(NetworkApi.MONTH_RECORD, map, new OkHttpManager.DataCallback() {
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
                        JSONArray monthRecord = object.getJSONArray("xunjian_day_list");
                        for (int i = monthRecord.length() - 1; i >= 0; i--) {
                            JSONObject data = (JSONObject) monthRecord.get(i);
                            String date = data.getString("total_date");
                            String number = data.getString("number");
                            String complete = data.getString("complete");
                            String percent = data.getString("percent");
                            Map<String, String> hashMap = new HashMap<>();
                            hashMap.put("date", date);
                            hashMap.put("number", number);
                            hashMap.put("complete", complete);
                            hashMap.put("percent", percent);
                            mDatas.add(hashMap);
                        }
                        mListView.setAdapter(new MonthRecordAdapter());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private class MonthRecordAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int i) {
            return mDatas.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = View.inflate(mContext, R.layout.month_record, null);
            }
            TextView tvDate = (TextView) view.findViewById(R.id.item_tv_date);
            TextView tvCompleteNumber = (TextView) view.findViewById(R.id.item_tv_complete_number);
            TextView tvTotalNumber = (TextView) view.findViewById(R.id.item_tv_total_number);
            TextView tvPercent = (TextView) view.findViewById(R.id.item_tv_percent);

            Map<String, String> map = mDatas.get(i);
            tvDate.setText(map.get("date"));
            tvCompleteNumber.setText(map.get("complete"));
            tvTotalNumber.setText(map.get("number"));
            tvPercent.setText(map.get("percent"));

            return view;
        }
    }

}

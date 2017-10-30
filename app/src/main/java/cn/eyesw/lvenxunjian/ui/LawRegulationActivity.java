package cn.eyesw.lvenxunjian.ui;

import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnItemClick;
import cn.eyesw.lvenxunjian.R;
import cn.eyesw.lvenxunjian.base.BaseActivity;
import cn.eyesw.lvenxunjian.utils.ToolbarUtil;

/**
 * 法律法规
 */
public class LawRegulationActivity extends BaseActivity {

    private List<String> mDatas;

    @BindView(R.id.law_toolbar)
    protected Toolbar mToolbar;
    @BindView(R.id.law_list_view)
    protected ListView mListView;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.activity_law_regulation;
    }

    @Override
    protected void initToolbar() {
        ToolbarUtil toolbarUtil = new ToolbarUtil(this);
        toolbarUtil.setToolbar(mToolbar, "法律法规");
    }

    @Override
    protected void initView() {
        mDatas = new ArrayList<>();
        mDatas.add("刑法");
        mDatas.add("管道保护法");
        mDatas.add("治安管理条例");

        mListView.setAdapter(new LawAdapter());
    }

    @OnItemClick(R.id.law_list_view)
    public void onItemClick(int position) {
        Intent intent = new Intent(mContext, RegulationListActivity.class);
        intent.putExtra("title", mDatas.get(position));
        startActivity(intent);
    }

    private class LawAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mDatas != null ? mDatas.size() : 0;
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
                view = View.inflate(mContext, R.layout.law_list_view, null);
            }
            TextView tvTitle = (TextView) view.findViewById(R.id.law_item_tv_title);
            tvTitle.setText(mDatas.get(i));
            return view;
        }
    }

}

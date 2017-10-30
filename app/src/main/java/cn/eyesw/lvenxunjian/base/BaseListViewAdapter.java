package cn.eyesw.lvenxunjian.base;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * ListView 适配器的基类(因为数据较少此处就不使用 ViewHolder 对 ListView 进行优化)
 */
public abstract class BaseListViewAdapter<T> extends BaseAdapter {

    private Context mContext;
    private List<T> mDatas;

    public BaseListViewAdapter(Context context, List<T> datas) {
        mContext = context;
        mDatas = datas;
    }

    @Override
    public int getCount() {
        return mDatas.size() != 0 ? mDatas.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(getAdapterLayoutRes(), parent, false);
        }
        T t = mDatas.get(position);
        initAdapterView(convertView, t);
        return convertView;
    }

    @LayoutRes
    protected abstract int getAdapterLayoutRes();

    protected abstract void initAdapterView(View view, T t);

}

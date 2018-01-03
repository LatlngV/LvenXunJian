package cn.eyesw.lvenxunjian.utils;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class ToolbarUtil {

    private AppCompatActivity mActivity;

    public ToolbarUtil(AppCompatActivity activity) {
        mActivity = activity;
    }

    public void setToolbar(Toolbar toolbar, String title) {
        mActivity.setSupportActionBar(toolbar);
        if (mActivity.getSupportActionBar() != null) {
            mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mActivity.getSupportActionBar().setTitle(title);
        }

        toolbar.setNavigationOnClickListener(view -> mActivity.finish());
    }

}

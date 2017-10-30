package cn.eyesw.lvenxunjian.ui;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.eyesw.lvenxunjian.R;
import cn.eyesw.lvenxunjian.base.BaseActivity;
import cn.eyesw.lvenxunjian.fragment.MonthRecordFragment;
import cn.eyesw.lvenxunjian.fragment.YearRecordFragment;

/**
 * 巡检记录
 */
public class PatrolRecordActivity extends BaseActivity {

    private String[] mTabTitle;
    private List<Fragment> mFragments;

    @BindView(R.id.record_toolbar)
    protected Toolbar mToolbar;
    @BindView(R.id.record_tab_layout)
    protected TabLayout mTabLayout;
    @BindView(R.id.record_view_pager)
    protected ViewPager mViewPager;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.activity_patrol_record;
    }

    @Override
    public void initToolbar() {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        mToolbar.setNavigationOnClickListener(view -> finish());
    }

    @Override
    protected void initView() {
        mFragments = new ArrayList<>();
        YearRecordFragment yearRecord = new YearRecordFragment();
        MonthRecordFragment monthRecord = new MonthRecordFragment();
        mFragments.add(monthRecord);
        mFragments.add(yearRecord);

        mTabTitle = new String[]{"月度巡检记录", "年度巡检记录"};
        mViewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mTabTitle.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabTitle[position];
        }
    }

}

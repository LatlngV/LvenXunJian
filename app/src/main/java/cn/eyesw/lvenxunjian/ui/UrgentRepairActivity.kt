package cn.eyesw.lvenxunjian.ui

import android.support.v4.app.Fragment
import cn.eyesw.lvenxunjian.R
import cn.eyesw.lvenxunjian.base.BaseActivity
import cn.eyesw.lvenxunjian.fragment.CompleteFragment
import cn.eyesw.lvenxunjian.fragment.NoCompleteFragment
import kotlinx.android.synthetic.main.activity_urgent_repair.*

/**
 * 抢维修
 */
class UrgentRepairActivity : BaseActivity() {

    private var mNoCompleteFragment: Fragment? = null
    private var mCompleteFragment: Fragment? = null

    override fun getContentLayoutRes(): Int = R.layout.activity_urgent_repair

    override fun initToolbar() {
        setSupportActionBar(urgent_repair_toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        // Toolbar 箭头的点击事件
        urgent_repair_toolbar.setNavigationOnClickListener{ finish() }
    }

    override fun initView() {
        // RadioGroup 点击事件
        urgent_repair_radio_group.setOnCheckedChangeListener { _, position ->
            when (position) {
                // 未完成
                R.id.urgent_repair_no_complete -> {
                    if (mNoCompleteFragment == null) {
                        mNoCompleteFragment = NoCompleteFragment()
                    }
                    replaceFragment(R.id.urgent_repair_fl_container, mNoCompleteFragment)
                }
                // 已完成
                R.id.urgent_repair_complete -> {
                    if (mCompleteFragment == null) {
                        mCompleteFragment = CompleteFragment()
                    }
                    replaceFragment(R.id.urgent_repair_fl_container, mCompleteFragment)
                }
            }
        }

        // 未完成的 Fragment
        mNoCompleteFragment = NoCompleteFragment()
        replaceFragment(R.id.urgent_repair_fl_container, mNoCompleteFragment)
    }

}

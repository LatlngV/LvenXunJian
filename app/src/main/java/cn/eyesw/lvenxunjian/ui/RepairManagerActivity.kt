package cn.eyesw.lvenxunjian.ui

import android.content.Intent
import android.os.Bundle
import cn.eyesw.lvenxunjian.R
import cn.eyesw.lvenxunjian.base.BaseActivity
import cn.eyesw.lvenxunjian.fragment.NormalFragment
import cn.eyesw.lvenxunjian.fragment.RepairFragment
import cn.eyesw.lvenxunjian.utils.ToolbarUtil
import kotlinx.android.synthetic.main.activity_repair_manager.*

/**
 * 维修上报
 */
class RepairManagerActivity : BaseActivity() {

    private var mNormalFragment: NormalFragment? = null
    private var mRepairFragment: RepairFragment? = null
    private var mFlag: Int = 0
    private var mTitle: String? = null
    private var mBundle: Bundle? = null

    override fun getContentLayoutRes(): Int = R.layout.activity_repair_manager

    override fun initToolbar() {
        mTitle = intent.getStringExtra("title")
        mFlag = intent.getIntExtra("flag", 0)

        val toolbarUtil = ToolbarUtil(this)
        toolbarUtil.setToolbar(repair_toolbar, mTitle)
    }

    override fun initView() {

        /* 进来默认加载正常的数据 */
        mRepairFragment = RepairFragment()
        mBundle = Bundle()
        mBundle!!.putString("title", mTitle)
        mBundle!!.putString("flag", mFlag.toString())
        mBundle!!.putString("type", "repair")
        mRepairFragment?.arguments = mBundle
        replaceFragment(R.id.repair_fl_container, mRepairFragment)

        // RadioGroup 监听事件
        repair_radio_group.setOnCheckedChangeListener { _, checkId ->
            when (checkId) {
                R.id.repair_normal -> {
                    if (mNormalFragment == null) {
                        mNormalFragment = NormalFragment()
                    }
                    if (!mNormalFragment!!.isAdded) {
                        mBundle = Bundle()
                        mBundle!!.putString("flag", mFlag.toString())
                        mBundle!!.putString("type", "repair")
                        mBundle!!.putString("title", mTitle)
                        mNormalFragment?.arguments = mBundle
                        replaceFragment(R.id.repair_fl_container, mNormalFragment)
                    }
                }
                R.id.repair_repair -> {
                    if (mRepairFragment == null) {
                        mRepairFragment = RepairFragment()
                    }
                    if (!mRepairFragment!!.isAdded) {
                        mBundle = Bundle()
                        mBundle!!.putString("flag", mFlag.toString())
                        mBundle!!.putString("type", "repair")
                        mBundle!!.putString("title", mTitle)
                        mRepairFragment?.arguments = mBundle
                        replaceFragment(R.id.repair_fl_container, mRepairFragment)
                    }
                }
            }
        }
        // 创建一条记录
        repair_create.setOnClickListener {
            // 跳转
            val intent = Intent(mContext, DangerDataActivity::class.java)
            intent.putExtra("status", "0") // 下一个界面需要根据传递的 status 显示对应数据
            intent.putExtra("title", mTitle)
            intent.putExtra("typeFlag", mFlag.toString())
            startActivity(intent)
        }
    }

}

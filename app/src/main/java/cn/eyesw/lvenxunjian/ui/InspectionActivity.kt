package cn.eyesw.lvenxunjian.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import cn.eyesw.lvenxunjian.R
import cn.eyesw.lvenxunjian.base.BaseActivity
import cn.eyesw.lvenxunjian.fragment.NormalFragment
import cn.eyesw.lvenxunjian.fragment.RepairFragment
import cn.eyesw.lvenxunjian.fragment.UpdateFragment
import cn.eyesw.lvenxunjian.utils.ToolbarUtil
import kotlinx.android.synthetic.main.activity_inspection.*

/**
 * 巡检上报
 */
class InspectionActivity : BaseActivity() {

    // 正常的 Fragment
    private var mNormalFragment: NormalFragment? = null
    // 建议更换的 Fragment
    private var mUpdateFragment: UpdateFragment? = null
    // 建议维修的 Fragment
    private var mRepairFragment: Fragment? = null
    // 标志位
    private var mFlag: Int? = null
    // Bundle 对象
    private var mBundle: Bundle? = null
    // 标题
    private var mTitle: String? = null

    override fun getContentLayoutRes(): Int = R.layout.activity_inspection

    override fun initToolbar() {
        mTitle = intent.getStringExtra("title")
        mFlag = intent.getIntExtra("flag", 0)

        val toolbarUtil = ToolbarUtil(this)
        toolbarUtil.setToolbar(inspection_toolbar, mTitle)
    }

    override fun initView() {
        mNormalFragment = NormalFragment()
        mUpdateFragment = UpdateFragment()
        mRepairFragment = RepairFragment()

        /* 进来默认显示正常的数据 */
        mBundle = Bundle()
        mBundle!!.putString("title", mTitle)
        mBundle!!.putString("flag", mFlag.toString())
        mBundle!!.putString("type", "inspection")
        mNormalFragment?.arguments = mBundle
        replaceFragment(R.id.inspection_fl_container, mNormalFragment)

        // RadioGroup 监听事件
        inspection_radio_group.setOnCheckedChangeListener { _, checkId ->
            when (checkId) {
                R.id.inspection_normal -> { // 正常的数据
                    if (mNormalFragment == null) {
                        mNormalFragment = NormalFragment()
                    }
                    if (!mNormalFragment!!.isAdded) {
                        mBundle = Bundle()
                        mBundle!!.putString("title", mTitle)
                        mBundle!!.putString("flag", mFlag.toString())
                        mBundle!!.putString("type", "inspection")
                        mNormalFragment?.arguments = mBundle
                        replaceFragment(R.id.inspection_fl_container, mNormalFragment)
                    }
                }
                R.id.inspection_repair -> { // 维修的数据
                    if (mRepairFragment == null) {
                        mRepairFragment = RepairFragment()
                    }
                    if (!mRepairFragment!!.isAdded) {
                        mBundle = Bundle()
                        mBundle!!.putString("title", mTitle)
                        mBundle!!.putString("flag", mFlag.toString())
                        mBundle!!.putString("type", "inspection")
                        mRepairFragment?.arguments = mBundle
                        replaceFragment(R.id.inspection_fl_container, mRepairFragment)
                    }
                }
                R.id.inspection_update -> { // 更换的数据
                    if (mUpdateFragment == null) {
                        mUpdateFragment = UpdateFragment()
                    }
                    if (!mUpdateFragment!!.isAdded) {
                        mBundle = Bundle()
                        mBundle!!.putString("title", mTitle)
                        mBundle!!.putString("flag", mFlag.toString())
                        mUpdateFragment?.arguments = mBundle
                        replaceFragment(R.id.inspection_fl_container, mUpdateFragment)
                    }
                }
            }
        }
        // 创建
        inspection_create.setOnClickListener {
            val intent = Intent(mContext, InspectionDataActivity::class.java)
            /* 状态: status {"0": "创建", "1": "记录"} */
            intent.putExtra("status", "0")
            intent.putExtra("typeFlag", mFlag.toString())
            intent.putExtra("title", mTitle)
            startActivity(intent)
        }
    }

}

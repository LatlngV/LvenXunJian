package cn.eyesw.lvenxunjian.ui

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import cn.eyesw.lvenxunjian.R
import cn.eyesw.lvenxunjian.base.BaseActivity
import cn.eyesw.lvenxunjian.base.BaseListViewAdapter
import cn.eyesw.lvenxunjian.bean.PatrolAreaEntity
import cn.eyesw.lvenxunjian.bean.StaffEntity
import cn.eyesw.lvenxunjian.utils.ToolbarUtil
import kotlinx.android.synthetic.main.activity_staff.*

class StaffActivity : BaseActivity() {

    private var mData: List<StaffEntity>? = null

    override fun getContentLayoutRes(): Int = R.layout.activity_staff

    override fun initToolbar() {
        val toolbarUtil = ToolbarUtil(this)
        toolbarUtil.setToolbar(staff_toolbar, "巡线人员")
    }

    override fun initView() {
        val datas = intent.getSerializableExtra("data") as PatrolAreaEntity
        mData = datas.list

        val adapter = StaffAdapter(mContext, mData as ArrayList<StaffEntity>)
        staff_list_view.adapter = adapter

        staff_list_view.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(mContext, HistoryTrackActivity::class.java)
            intent.putExtra("id", (mData as ArrayList<StaffEntity>)[position].id)
            startActivity(intent)
        }
    }

    private inner class StaffAdapter(ctx: Context, list: List<StaffEntity>) : BaseListViewAdapter<StaffEntity>(ctx, list) {
        override fun getAdapterLayoutRes(): Int = R.layout.law_list_view

        override fun initAdapterView(view: View?, t: StaffEntity?) {
            val textView = view?.findViewById<TextView>(R.id.law_item_tv_title)
            textView?.text = t?.name
        }

    }

}

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
import cn.eyesw.lvenxunjian.utils.NetWorkUtil
import cn.eyesw.lvenxunjian.utils.SpUtil
import cn.eyesw.lvenxunjian.utils.ToolbarUtil
import kotlinx.android.synthetic.main.activity_patrol_area.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class PatrolAreaActivity : BaseActivity() {

    private val mPatrolAreaList = ArrayList<PatrolAreaEntity>()
    private var mStaffList: ArrayList<StaffEntity>? = null

    override fun getContentLayoutRes(): Int = R.layout.activity_patrol_area

    override fun initToolbar() {
        val toolbarUtil = ToolbarUtil(this)
        toolbarUtil.setToolbar(area_toolbar, "巡线区")
    }

    override fun initView() {
        val apiService = NetWorkUtil.getInstance().apiService
        val patrolArea = apiService.patrolArea(SpUtil.getInstance(mContext).getString("id"))
        patrolArea.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                val json = String(response?.body()!!.bytes())
                val jsonObject = JSONObject(json)
                val code = jsonObject.getInt("code")
                if (code == 200) {
                    val jsonArray = jsonObject.getJSONObject("data").getJSONObject("result").getJSONArray("line_area")
                    for (i in 0 until jsonArray.length()) {
                        val patrolArea = jsonArray[i] as JSONObject
                        val id = patrolArea.getString("id")
                        val name = patrolArea.getString("name")
                        val staffArray = patrolArea.getJSONArray("staff")
                        mStaffList = ArrayList()
                        for (j in 0 until staffArray.length()) {
                            val staff = staffArray[j] as JSONObject
                            val staffId = staff.getString("staff_id")
                            val staffName = staff.getString("staff_name")
                            val staffEntity = StaffEntity(staffId, staffName)
                            mStaffList!!.add(staffEntity)
                        }
                        val patrolAreaEntity = PatrolAreaEntity(id, name, mStaffList)
                        mPatrolAreaList.add(patrolAreaEntity)
                    }
                    val adapter = PatrolAreaAdapter(mContext, mPatrolAreaList)
                    area_list_view.adapter = adapter
                }
                call?.cancel()
            }

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                call?.cancel()
                showToast("网络连接失败")
            }

        })

        area_list_view.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(mContext, StaffActivity::class.java)
            intent.putExtra("data", mPatrolAreaList[position])
            startActivity(intent)
        }
    }

    private inner class PatrolAreaAdapter(ctx: Context, list: List<PatrolAreaEntity>) : BaseListViewAdapter<PatrolAreaEntity>(ctx, list) {
        override fun getAdapterLayoutRes(): Int = R.layout.law_list_view

        override fun initAdapterView(view: View?, t: PatrolAreaEntity?) {
            val textView = view?.findViewById<TextView>(R.id.law_item_tv_title)
            textView?.text = t?.name
        }

    }

}

package cn.eyesw.lvenxunjian.fragment

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import cn.eyesw.lvenxunjian.R
import cn.eyesw.lvenxunjian.base.BaseFragment
import cn.eyesw.lvenxunjian.base.BaseListViewAdapter
import cn.eyesw.lvenxunjian.bean.DataInfoEntity
import cn.eyesw.lvenxunjian.ui.DangerDataActivity
import cn.eyesw.lvenxunjian.ui.InspectionDataActivity
import cn.eyesw.lvenxunjian.utils.NetWorkUtil
import cn.eyesw.lvenxunjian.utils.SpUtil
import kotlinx.android.synthetic.main.fragment_repair.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 建议维修的 Fragment
 */
class RepairFragment : BaseFragment() {

    private var mFlag: String? = null
    private var mType: String? = null
    private val mList = mutableListOf<DataInfoEntity>()

    override fun getContentLayoutRes(): Int = R.layout.fragment_repair

    override fun initView() {
        mFlag = arguments?.getString("flag")
        mType = arguments?.getString("type")
        val title = arguments?.getString("title")

        val spUtil = SpUtil.getInstance(mContext)
        mList.clear()

        val apiService = NetWorkUtil.getInstance().apiService
        val dataReport = apiService.dataReport(spUtil.getString("id"), mFlag, "1")
        dataReport.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                Toast.makeText(mContext, "网络连接失败", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                val json = String(response?.body()?.bytes()!!)
                // 解析数据
                analysisData(json)
            }

        })
        repair_list_view.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val dataId = mList[position].dataId
            var intent: Intent? = null
            if (mType == "repair") {
                intent = Intent(mContext, DangerDataActivity::class.java)
                intent.putExtra("status", "1") // 0 代表已完成，1 代表维修中
            } else if (mType == "inspection") {
                intent = Intent(mContext, InspectionDataActivity::class.java)
                intent.putExtra("status", "1")
            }
            intent?.putExtra("title", title)
            intent?.putExtra("dataId", dataId)
            intent?.putExtra("typeFlag", mFlag)
            startActivity(intent)
        }
    }

    /**
     * 解析 json 数据
     */
    private fun analysisData(json: String) {
        val jsonObject = JSONObject(json)
        val code = jsonObject.getInt("code")
        if (code == 200) {
            val jsonArray = jsonObject.getJSONObject("data").getJSONArray("result")
            if (jsonArray.length() == 0) {
                repair_list_view.visibility = View.GONE
                repair_tv_empty.visibility = View.VISIBLE
            } else {
                repair_list_view.visibility = View.VISIBLE
                repair_tv_empty.visibility = View.GONE
                for (i in 0 until jsonArray.length()) {
                    val data = jsonArray[i] as JSONObject
                    val address = data.getString("address")
                    val createTime = data.getString("createtime")
                    val dataId = data.getString("data_id")
                    mList.add(DataInfoEntity(dataId, createTime, address))
                }
                val adapter = NormalAdapter(mContext, mList)
                repair_list_view.adapter = adapter
            }
        }
    }

    /**
     * 适配器
     */
    private inner class NormalAdapter(context: Context?, datas: MutableList<DataInfoEntity>?) : BaseListViewAdapter<DataInfoEntity>(context, datas) {
        override fun getAdapterLayoutRes(): Int = R.layout.item_data_info

        override fun initAdapterView(view: View?, t: DataInfoEntity?) {
            view?.findViewById<TextView>(R.id.data_info_tv_create_time)?.text = t?.createTime
            view?.findViewById<TextView>(R.id.data_info_tv_address)?.text = t?.address
            if (mType == "repair") {
                view?.findViewById<TextView>(R.id.data_info_tv_status)?.text = "维修中"
            } else {
                view?.findViewById<TextView>(R.id.data_info_tv_status)?.visibility = View.GONE
            }
        }

    }


}

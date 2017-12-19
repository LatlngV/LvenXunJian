package cn.eyesw.lvenxunjian.fragment

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import cn.eyesw.lvenxunjian.R
import cn.eyesw.lvenxunjian.base.BaseFragment
import cn.eyesw.lvenxunjian.base.BaseListViewAdapter
import cn.eyesw.lvenxunjian.bean.DataInfoEntity
import cn.eyesw.lvenxunjian.ui.InspectionDataActivity
import cn.eyesw.lvenxunjian.utils.NetWorkUtil
import cn.eyesw.lvenxunjian.utils.SpUtil
import kotlinx.android.synthetic.main.fragment_update.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 建议更换的 Fragment
 */
class UpdateFragment : BaseFragment() {

    private val mList = mutableListOf<DataInfoEntity>()

    override fun getContentLayoutRes(): Int = R.layout.fragment_update

    override fun initView() {
        val flag = arguments?.getString("flag")

        mList.clear()
        val spUtil = SpUtil.getInstance(mContext)

        val apiService = NetWorkUtil.getInstance().apiService
        val dataReport = apiService.dataReport(spUtil.getString("id"), flag, "2")
        dataReport.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                update_list_view.visibility = View.GONE
                update_tv_empty.visibility = View.VISIBLE
                update_tv_empty.text = getString(R.string.network_error)
            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                val json = String(response?.body()?.bytes()!!)
                val jsonObject = JSONObject(json)
                val code = jsonObject.getInt("code")
                if (code == 200) {
                    val jsonArray = jsonObject.getJSONObject("data").getJSONArray("result")
                    if (jsonArray.length() == 0) {
                        update_list_view.visibility = View.GONE
                        update_tv_empty.visibility = View.VISIBLE
                    } else {
                        update_list_view.visibility = View.VISIBLE
                        update_tv_empty.visibility = View.GONE
                        for (i in 0 until jsonArray.length()) {
                            val data = jsonArray[i] as JSONObject
                            val address = data.getString("address")
                            val createTime = data.getString("createtime")
                            val dataId = data.getString("data_id")
                            mList.add(DataInfoEntity(dataId, createTime, address))
                        }
                        val adapter = UpdateAdapter(mContext, mList)
                        update_list_view.adapter = adapter
                    }
                }
            }

        })

        // ListView item 的点击事件
        update_list_view.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val dataId = mList[position].dataId
            val intent = Intent(mContext, InspectionDataActivity::class.java)
            intent.putExtra("dataId", dataId)
            intent.putExtra("status", "1")
            startActivity(intent)
        }
    }

    /**
     * 适配器
     */
    private inner class UpdateAdapter(context: Context?, datas: MutableList<DataInfoEntity>?) : BaseListViewAdapter<DataInfoEntity>(context, datas) {
        override fun getAdapterLayoutRes(): Int = R.layout.item_data_info

        override fun initAdapterView(view: View?, t: DataInfoEntity?) {
            view?.findViewById<TextView>(R.id.data_info_tv_create_time)?.text = t?.createTime
            view?.findViewById<TextView>(R.id.data_info_tv_address)?.text = t?.address
            view?.findViewById<TextView>(R.id.data_info_tv_status)?.visibility = View.GONE
        }

    }

}

package cn.eyesw.lvenxunjian.fragment

import android.content.Intent
import android.view.View
import android.widget.Toast
import cn.eyesw.lvenxunjian.R
import cn.eyesw.lvenxunjian.base.BaseFragment
import cn.eyesw.lvenxunjian.bean.RepairManagerEntity
import cn.eyesw.lvenxunjian.utils.NetWorkUtil
import cn.eyesw.lvenxunjian.utils.SpUtil
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.json.JSONObject
import android.widget.TextView
import android.view.ViewGroup
import android.widget.BaseAdapter
import cn.eyesw.lvenxunjian.constant.Constant
import cn.eyesw.lvenxunjian.ui.StaffDataActivity
import kotlinx.android.synthetic.main.fragment_complete.*

/**
 * 已完成
 */
class CompleteFragment : BaseFragment() {

    private val mList = mutableListOf<RepairManagerEntity>()
    private val mDidList = mutableListOf<String>()

    override fun getContentLayoutRes(): Int = R.layout.fragment_complete

    override fun initView() {
        val apiService = NetWorkUtil.getInstance().apiService
        val repairList = apiService.repairList(SpUtil.getInstance(mContext).getString("id"))
        repairList.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                val json = String(response?.body()?.bytes()!!)
                val jsonObject = JSONObject(json)
                val code = jsonObject.getInt("code")
                if (code == 200) {
                    val weixiuList = jsonObject.getJSONArray("weixiu_list")
                    for (i in 0 until weixiuList.length()) {
                        val data = weixiuList.get(i) as JSONObject
                        val did = data.getString("did")
                        var dangerType = data.getString("type_name")
                        val dangerLevel = data.getString("level_name")
                        val staffName = data.getString("staff_name")
                        val dnote = data.getString("dnote")
                        val address = data.getString("addr")
                        var ctime = data.getString("completetime")
                        val status = data.getString("status")
                        val managerName = data.getString("manager_name")

                        if (dangerType == "null") {
                            dangerType = ""
                        }
                        if (ctime == "null") {
                            ctime = ""
                        }
                        val repairMangerEntity = RepairManagerEntity(did, dangerType, dangerLevel, staffName, dnote,
                                address, ctime, managerName, "", "")
                        if (status == "2") {
                            mList.add(repairMangerEntity)
                        }
                        mDidList.add(did)
                    }
                    if (mList.size > 0) {
                        val adapter = CompleteMessageAdapter()
                        complete_list_view.adapter = adapter

                        complete_list_view.setOnItemClickListener { _, _, p2, _ ->
                            val repairManagerEntity = mList[p2]
                            val intent = Intent(mContext, StaffDataActivity::class.java)
                            intent.putExtra(Constant.DANGER_DATA_FLAG, "complete")
                            intent.putExtra("did", mDidList[p2])
                            intent.putExtra("staff_name", repairManagerEntity.staffName)
                            intent.putExtra("addr", repairManagerEntity.address)
                            intent.putExtra("dnote", repairManagerEntity.dnote)
                            intent.putExtra("ctime", repairManagerEntity.ctime)
                            startActivity(intent)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                Toast.makeText(mContext, "网络连接失败", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private inner class CompleteMessageAdapter : BaseAdapter() {

        override fun getCount(): Int {
            return if (mList.size > 0) mList.size else 0
        }

        override fun getItem(position: Int): Any {
            return mList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            val view = View.inflate(mContext, R.layout.item_complete_message, null)

            val tvCard = view?.findViewById<TextView>(R.id.item_tv_card)
            val tvCreateTime = view?.findViewById<TextView>(R.id.item_tv_create_time)
            val tvDangerType = view?.findViewById<TextView>(R.id.item_tv_danger_type)
            val tvAddress = view?.findViewById<TextView>(R.id.item_tv_address)
            val tvTime = view?.findViewById<TextView>(R.id.item_tv_time)
            val tvUploadName = view?.findViewById<TextView>(R.id.item_tv_upload_name)
            val tvRepairNames = view?.findViewById<TextView>(R.id.item_tv_repair_name)

            val repairMangerEntity = mList[position]
            tvCard?.text = "已完成任务"
            tvTime?.text = "完成时间："
            tvUploadName?.text = repairMangerEntity.staffName
            tvRepairNames?.text = repairMangerEntity.managerName
            tvCreateTime?.text = repairMangerEntity.ctime
            tvDangerType?.text = repairMangerEntity.dangerType
            tvAddress?.text = repairMangerEntity.address
            return view
        }
    }

}

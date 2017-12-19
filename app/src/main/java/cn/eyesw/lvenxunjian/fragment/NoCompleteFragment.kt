package cn.eyesw.lvenxunjian.fragment

import android.content.Intent
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import cn.eyesw.lvenxunjian.R
import cn.eyesw.lvenxunjian.base.BaseFragment
import cn.eyesw.lvenxunjian.bean.RepairManagerEntity
import cn.eyesw.lvenxunjian.constant.Constant
import cn.eyesw.lvenxunjian.ui.DangerDataActivity
import cn.eyesw.lvenxunjian.ui.StaffDataActivity
import cn.eyesw.lvenxunjian.utils.NetWorkUtil
import cn.eyesw.lvenxunjian.utils.SpUtil
import kotlinx.android.synthetic.main.fragment_no_complete.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 未完成
 */
class NoCompleteFragment : BaseFragment() {

    private val mList = mutableListOf<RepairManagerEntity>()
    private val mDidList = mutableListOf<String>()

    override fun getContentLayoutRes(): Int = R.layout.fragment_no_complete

    override fun initView() {
        val apiService = NetWorkUtil.getInstance().apiService
        val repairList = apiService.repairList(SpUtil.getInstance(mContext).getString("id"))
        repairList.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                val json = String(response?.body()?.bytes()!!)
                Log.d("tag", json)
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
                        val longitude = data.getString("longitude")
                        val latitude = data.getString("latitude")

                        if (dangerType == "null") {
                            dangerType = ""
                        }
                        if (ctime == "null") {
                            ctime = ""
                        }
                        val repairMangerEntity = RepairManagerEntity(did, dangerType, dangerLevel, staffName, dnote,
                                address, ctime, managerName, latitude, longitude)
                        val roleName = SpUtil.getInstance(mContext).getString("roleName")
                        val repairName = SpUtil.getInstance(mContext).getString("staffName")
                        if (roleName == "维修人员" && managerName == repairName) {
                            mList.add(repairMangerEntity)
                        } else if (status == "1" || status == "0") {
                            mList.add(repairMangerEntity)
                        }
                        mDidList.add(did)
                    }
                    if (mList.size > 0) {
                        val adapter = NoCompleteMessageAdapter()
                        no_complete_list_view.adapter = adapter

                        no_complete_list_view.setOnItemClickListener { _, _, p2, _ ->
                            val repairManagerEntity = mList[p2]
                            val intent = Intent(mContext, StaffDataActivity::class.java)
                            intent.putExtra(Constant.DANGER_DATA_FLAG, "noComplete")
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

    private inner class NoCompleteMessageAdapter : BaseAdapter() {

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

            val tvNumber = view?.findViewById<TextView>(R.id.item_tv_number)
            val tvUploadName = view?.findViewById<TextView>(R.id.item_tv_upload_name)
            val tvRepairName = view?.findViewById<TextView>(R.id.item_tv_repair_name)
            val tvCreateTime = view?.findViewById<TextView>(R.id.item_tv_create_time)
            val tvDangerType = view?.findViewById<TextView>(R.id.item_tv_danger_type)
            val tvAddress = view?.findViewById<TextView>(R.id.item_tv_address)
            val tvNoAppoint = view?.findViewById<TextView>(R.id.item_tv_no_appoint)
            val dangerManagerEntity = mList[position]
            tvUploadName?.text = dangerManagerEntity.staffName

            if (dangerManagerEntity.managerName == "未设定") {
                tvNoAppoint?.visibility = View.VISIBLE
                tvRepairName?.text = dangerManagerEntity.managerName
            } else {
                tvRepairName?.text = dangerManagerEntity.managerName
                tvNoAppoint?.visibility = View.GONE
            }
            tvCreateTime?.text = dangerManagerEntity.ctime
            tvDangerType?.text = dangerManagerEntity.dangerType
            tvAddress?.text = dangerManagerEntity.address
            tvNumber?.text = (position + 1).toString()

            return view
        }
    }


}

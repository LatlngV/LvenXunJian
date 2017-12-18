package cn.eyesw.lvenxunjian.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import cn.eyesw.lvenxunjian.R
import cn.eyesw.lvenxunjian.base.BaseActivity
import cn.eyesw.lvenxunjian.base.BaseListViewAdapter
import cn.eyesw.lvenxunjian.bean.DataCollectionEntity
import cn.eyesw.lvenxunjian.utils.NetWorkUtil
import cn.eyesw.lvenxunjian.utils.SpUtil
import cn.eyesw.lvenxunjian.utils.ToolbarUtil
import kotlinx.android.synthetic.main.activity_data_collection.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 数据采集
 */
class DataCollectionActivity : BaseActivity() {

    private var mList = mutableListOf<DataCollectionEntity>()

    override fun getContentLayoutRes(): Int = R.layout.activity_data_collection

    override fun initToolbar() {
        val toolbarUtil = ToolbarUtil(this)
        toolbarUtil.setToolbar(data_collection_toolbar, "数据采集")
    }

    override fun initView() {
        mList.add(DataCollectionEntity(R.drawable.data_collection1, "警示牌"))
        mList.add(DataCollectionEntity(R.drawable.data_collection2, "标志桩"))
        mList.add(DataCollectionEntity(R.drawable.data_collection3, "阴保桩"))
        mList.add(DataCollectionEntity(R.drawable.data_collection4, "水工保护"))
        mList.add(DataCollectionEntity(R.drawable.data_collection7, "光纤"))
        mList.add(DataCollectionEntity(R.drawable.data_collection10, "阀室"))
        mList.add(DataCollectionEntity(R.drawable.data_collection8, "管道维护"))
        mList.add(DataCollectionEntity(R.drawable.data_collection9, "场站维护"))
        mList.add(DataCollectionEntity(R.drawable.data_collection5, "其他"))

        val adapter = DataCollectionAdapter(mContext, mList)
        data_collection_list_view.adapter = adapter

        data_collection_list_view.setOnItemClickListener { _, _, position, _ ->
            val title = mList[position].title
            var intent: Intent? = null
            when (position) {
                0 -> {
                    intent = Intent(mContext, InspectionActivity::class.java)
                    intent.putExtra("flag", 1)
                }
                1 -> {
                    intent = Intent(mContext, InspectionActivity::class.java)
                    intent.putExtra("flag", 2)
                }
                2 -> {
                    intent = Intent(mContext, InspectionActivity::class.java)
                    intent.putExtra("flag", 3)
                }
                3 -> {
                    intent = Intent(mContext, InspectionActivity::class.java)
                    intent.putExtra("flag", 4)
                }
                4 -> {
                    intent = Intent(mContext, RepairManagerActivity::class.java)
                    intent.putExtra("flag", 7)
                }
                5 -> {
                    intent = Intent(mContext, RepairManagerActivity::class.java)
                    intent.putExtra("flag", 10)
                }
                6 -> {
                    intent = Intent(mContext, RepairManagerActivity::class.java)
                    intent.putExtra("flag", 8)
                }
                7 -> {
                    intent = Intent(mContext, RepairManagerActivity::class.java)
                    intent.putExtra("flag", 9)
                }
                8 -> {
                    intent = Intent(mContext, InspectionActivity::class.java)
                    intent.putExtra("flag", 5)
                }
            }
            intent?.putExtra("title", title)
            startActivity(intent)
        }
    }

    /**
     * 数据采集适配器
     */
    private class DataCollectionAdapter(context: Context, array: List<DataCollectionEntity>) : BaseListViewAdapter<DataCollectionEntity>(context, array) {
        override fun getAdapterLayoutRes(): Int = R.layout.item_data_collection

        override fun initAdapterView(view: View?, entity: DataCollectionEntity?) {
            view?.findViewById<ImageView>(R.id.iv_item_collection)!!.setBackgroundResource(entity?.resId!!)
            view.findViewById<TextView>(R.id.tv_item_collection)!!.text = entity.title
        }
    }

}

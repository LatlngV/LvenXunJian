package cn.eyesw.lvenxunjian.ui

import android.view.View
import cn.eyesw.lvenxunjian.R
import cn.eyesw.lvenxunjian.base.BaseActivity
import cn.eyesw.lvenxunjian.constant.NetworkApi
import cn.eyesw.lvenxunjian.utils.ToolbarUtil
import kotlinx.android.synthetic.main.activity_history_track.*

class HistoryTrackActivity : BaseActivity() {

    override fun getContentLayoutRes(): Int = R.layout.activity_history_track

    override fun initToolbar() {
        val toolbarUtil = ToolbarUtil(this)
        toolbarUtil.setToolbar(history_toolbar, "历史轨迹")
    }

    override fun initView() {
        val staffId = intent.getStringExtra("id")
        val settings = history_web_view.settings
        settings.javaScriptEnabled = true
        history_web_view.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        history_web_view.loadUrl(NetworkApi.ROUTE_MAP + "?staff_id=" + staffId)
    }

    override fun onResume() {
        super.onResume()
        history_web_view.onResume()
        history_web_view.resumeTimers()
    }

    override fun onPause() {
        super.onPause()
        history_web_view.onPause()
        history_web_view.pauseTimers()
    }

    override fun onDestroy() {
        history_web_view.stopLoading()
        history_web_view.destroy()
        super.onDestroy()
    }

}

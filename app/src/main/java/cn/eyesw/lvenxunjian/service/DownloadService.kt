package cn.eyesw.lvenxunjian.service

import android.app.DownloadManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import java.io.File
import java.lang.Exception
import android.webkit.MimeTypeMap
import android.widget.Toast

class DownloadService : Service() {

    private var mDownloadManager: DownloadManager? = null
    private var mDownloadCompleteReceiver: DownloadCompleteReceiver? = null
    private var mUrl: String? = null
    private val path = "/apk/"

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mUrl = intent?.getStringExtra("url")
        val path = Environment.getExternalStorageDirectory().absolutePath + path + "inspection.apk"
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
        try {
            intiDownloadManager()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Service.START_NOT_STICKY
    }

    private fun intiDownloadManager() {
        mDownloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        mDownloadCompleteReceiver = DownloadCompleteReceiver()
        // 设置下载地址
        val down = DownloadManager.Request(Uri.parse(mUrl))
        // 设置允许使用的网络类型，这里是移动网络和 wifi 都可以
        down.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
        down.setAllowedOverRoaming(false)
        val mimeTypeMap = MimeTypeMap.getSingleton()
        val mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(mUrl))
        down.setMimeType(mimeString)
        // 下载时，通知栏显示途中
        down.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        // 显示下载界面
        down.setVisibleInDownloadsUi(true)
        // 设置下载后文件存放的位置
        down.setDestinationInExternalPublicDir(path, "inspection.apk")
        down.setTitle("正在下载")
        // 将下载请求放入队列
        mDownloadManager!!.enqueue(down)
        //注册下载广播
        registerReceiver(mDownloadCompleteReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onDestroy() {
        if (mDownloadCompleteReceiver != null) {
            unregisterReceiver(mDownloadCompleteReceiver)
            mDownloadCompleteReceiver = null
        }
        super.onDestroy()
    }

    private inner class DownloadCompleteReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // 判断是否下载完成的广播
            if (intent?.action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                // 获取下载的文件 id
                val downId = intent!!.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (mDownloadManager!!.getUriForDownloadedFile(downId) != null) {
                    // 自动安装 apk
                    installAPK(mDownloadManager!!.getUriForDownloadedFile(downId))
                    // installAPK(context);
                } else {
                    Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show()
                }
                // 停止服务并关闭广播
                this@DownloadService.stopSelf()
            }
        }
    }

    private fun installAPK(apk: Uri) {
        val intents = Intent()
        intents.action = "android.intent.action.VIEW"
        intents.addCategory("android.intent.category.DEFAULT")
        intents.type = "application/vnd.android.package-archive"
        intents.data = apk
        intents.setDataAndType(apk, "application/vnd.android.package-archive")
        intents.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intents)
    }

}

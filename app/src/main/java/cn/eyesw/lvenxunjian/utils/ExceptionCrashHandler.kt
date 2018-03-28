package cn.eyesw.lvenxunjian.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat

/**
 * 收集程序崩溃信息
 */
class ExceptionCrashHandler private constructor() : Thread.UncaughtExceptionHandler {

    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null
    private var mContext: Context? = null

    companion object {

        @Volatile
        private var mExceptionCrashHandler: ExceptionCrashHandler? = null

        fun getInstance(): ExceptionCrashHandler {
            if (mExceptionCrashHandler == null) {
                synchronized(ExceptionCrashHandler::class.java) {
                    if (mExceptionCrashHandler == null) {
                        mExceptionCrashHandler = ExceptionCrashHandler()
                    }
                }
            }
            return mExceptionCrashHandler!!
        }
    }

    fun init(context: Context) {
        Thread.currentThread().uncaughtExceptionHandler = this
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        mContext = context
    }

    override fun uncaughtException(t: Thread?, e: Throwable?) {
        val crashFileName = saveInfoIoSD(e)
        cacheCrashFile(crashFileName)
        mDefaultHandler!!.uncaughtException(t, e)
    }

    /**
     * 保存获取的软件信息，设备信息和出错信息保存在 SDCard 中
     */
    private fun saveInfoIoSD(e: Throwable?): String {
        var fileName: String? = null
        val sb = StringBuffer()

        for (entry in obtainSimpleInfo(mContext!!).entries) {
            val key = entry.key
            val value = entry.value
            sb.append(key).append(" = ").append(value).append("\n")
        }

        sb.append(obtainExceptionInfo(e!!))

        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val dir = File(mContext?.filesDir!!.absolutePath + File.separator + "crash" + File.separator)

            // 先删除之前的异常信息
            if (dir.exists()) {
                deleteDir(dir)
            }

            // 再从新创建文件夹
            if (!dir.exists()) {
                dir.mkdir()
            }
            try {
                fileName = dir.absolutePath + File.separator + getAssignTime("yyyy_MM_dd_HH_mm") + ".txt"
                val fos = FileOutputStream(fileName)
                fos.write(sb.toString().toByteArray())
                fos.flush()
                fos.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return fileName!!
    }

    /**
     *  获取系统未捕捉的错误信息
     */
    private fun obtainExceptionInfo(throwable: Throwable): String {
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        throwable.printStackTrace(printWriter)
        printWriter.close()
        return stringWriter.toString()
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     */
    private fun deleteDir(dir: File) {
        if (dir.isDirectory) {
            val files = dir.listFiles()
            // 递归删除目录中的子目录下
            for (file in files) {
                if (file.isFile) {
                    file.delete()
                } else {
                    deleteDir(file)
                }
            }
        }
    }

    /**
     * 根据格式返回当前日期
     */
    private fun getAssignTime(s: String): String {
        val dataFormat = SimpleDateFormat(s)
        val currentTime: Long = System.currentTimeMillis()
        return dataFormat.format(currentTime)
    }

    /**
     * 获取一些简单的信息,软件版本、手机版本、型号等信息存放在 HashMap 中
     */
    private fun obtainSimpleInfo(context: Context): HashMap<String, String> {
        val map = HashMap<String, String>()
        val packageManager = context.packageManager
        var packageInfo: PackageInfo? = null
        try {
            packageInfo = packageManager.getPackageInfo(context.packageName, PackageManager.GET_ACTIVITIES)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        map["versionName"] = packageInfo!!.versionName
        map["versionCode"] = "" + packageInfo.versionCode
        map["MODEL"] = "" + Build.MODEL
        map["SDK_INT"] = "" + Build.VERSION.SDK_INT
        map["PRODUCT"] = "" + Build.PRODUCT
        map["MOBILE_INFO"] = getMobileInfo()
        return map
    }

    /**
     * 获取手机信息
     */
    private fun getMobileInfo(): String {
        val sb = StringBuffer()
        try {
            val fields = Build::class.java.declaredFields
            for (field in fields) {
                field.isAccessible = true
                val name = field.name
                val value = field.get(null).toString()
                sb.append(name + "=" + value)
                sb.append("\n")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sb.toString()
    }

    private fun cacheCrashFile(crashFileName: String) {
        SpUtil.getInstance(mContext).putString("crashFileName", crashFileName)
    }

    /**
     * 获取崩溃文件名称
     */
    fun getCrashFile(): File {
        val crashFileName = SpUtil.getInstance(mContext).getString("crashFileName")
        return File(crashFileName)
    }

}
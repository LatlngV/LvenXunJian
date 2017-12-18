package cn.eyesw.lvenxunjian.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;

import cn.eyesw.lvenxunjian.R;

public class UpdateManager {

    private Context mContext;

    private String apkUrl;

    /* 下载包安装路径 */
    private static final String savePath = "/sdcard/updatedemo/";

    private static final String saveFileName = savePath + "UpdateDemoRelease.apk";

    /* 进度条与通知 UI 刷新的 handler 和 msg 常量 */
    private ProgressBar mProgress;

    private static final int DOWN_UPDATE = 1;
    private static final int DOWN_OVER = 2;

    private int progress;

    private boolean interceptFlag = false;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    mProgress.setProgress(progress);
                    break;
                case DOWN_OVER:
                    installApk();
                    break;
                default:
                    break;
            }
        }
    };

    public UpdateManager(Context context, String url) {
        mContext = context;
        apkUrl = url;
    }

    // 外部接口让主 Activity 调用
    public void checkUpdateInfo() {
        showDownloadDialog();
    }

    private void showDownloadDialog() {
        Builder builder = new Builder(mContext);
        builder.setTitle("版本更新");

        View v = View.inflate(mContext, R.layout.progress, null);
        mProgress = v.findViewById(R.id.progress);

        builder.setView(v);
        builder.setNegativeButton("取消", (dialog, which) -> {
            dialog.dismiss();
            interceptFlag = true;
        });
        Dialog downloadDialog = builder.create();
        downloadDialog.setCancelable(false);
        downloadDialog.show();

        downloadApk();
    }

    /**
     * 下载apk
     */
    private void downloadApk() {
        Thread downLoadThread = new Thread(mdownApkRunnable);
        downLoadThread.start();
    }

    private Runnable mdownApkRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                URL url = new URL(apkUrl);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();

                File file = new File(savePath);
                if (!file.exists()) {
                    file.mkdir();
                }
                String apkFile = saveFileName;
                File mApkFile = new File(apkFile);
                FileOutputStream fos = new FileOutputStream(mApkFile);

                int count = 0;
                byte[] buf = new byte[1024];

                do {
                    int numread = is.read(buf);
                    count += numread;
                    progress = (int) (((float) count / length) * 100);
                    // 更新进度
                    mHandler.sendEmptyMessage(DOWN_UPDATE);
                    if (numread <= 0) {
                        // 下载完成通知安装
                        mHandler.sendEmptyMessage(DOWN_OVER);
                        break;
                    }
                    fos.write(buf, 0, numread);
                } while (!interceptFlag);// 点击取消就停止下载.

                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 安装apk
     */
    private void installApk() {
        File apkfile = new File(saveFileName);
        if (!apkfile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(i);
    }

}
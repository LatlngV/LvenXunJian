package cn.eyesw.lvenxunjian.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.List;

import cn.eyesw.lvenxunjian.bean.PictureBean;
import cn.eyesw.lvenxunjian.service.UploadPictureService;
import cn.eyesw.lvenxunjian.utils.PictureDao;

/**
 * 监听网络状态改变的广播
 */
public class NetWorkReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            // 得到连接管理器对象
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                PictureDao pictureDao = new PictureDao(context);
                List<PictureBean> list = pictureDao.query();
                Intent service = new Intent(context, UploadPictureService.class);
                if (list != null && list.size() > 0) {
                    context.startService(service);
                } else {
                    context.stopService(service);
                }
            }
        }
    }

}

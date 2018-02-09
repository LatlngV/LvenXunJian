package cn.eyesw.lvenxunjian.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.eyesw.greendao.PhotoBeanDao;
import cn.eyesw.lvenxunjian.LvenXunJianApplication;
import cn.eyesw.lvenxunjian.bean.PhotoBean;
import cn.eyesw.lvenxunjian.bean.PictureBean;
import cn.eyesw.lvenxunjian.constant.NetworkApi;
import cn.eyesw.lvenxunjian.utils.OkHttpManager;
import cn.eyesw.lvenxunjian.utils.PictureDao;
import cn.eyesw.lvenxunjian.utils.SpUtil;
import okhttp3.Call;

/**
 * 联网状态下如果数据库有数据就把数据上传到服务器
 */
public class UploadPictureService extends Service {

    private SpUtil mSpUtil;
    private OkHttpManager mOkHttpManager;

    public UploadPictureService() {
        mSpUtil = SpUtil.getInstance(this);
        mOkHttpManager = OkHttpManager.getInstance();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PhotoBeanDao photoBeanDao = LvenXunJianApplication.getDaoSession().getPhotoBeanDao();
        List<PhotoBean> list = photoBeanDao.queryBuilder().list();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                PhotoBean pictureBean = list.get(i);
                String picture = pictureBean.getBitmap();
                String longitude = pictureBean.getLongitude();
                String latitude = pictureBean.getLatitude();
                String date = pictureBean.getDate();
                String fileName = pictureBean.getFileName();

                Map<String, String> map = new HashMap<>();
                map.put("file", picture);
                map.put("task_id", mSpUtil.getString("task_id"));
                map.put("staff_id", mSpUtil.getString("id"));
                map.put("photo_time", date);
                map.put("longitude", longitude);
                map.put("latitude", latitude);
                map.put("file_name", fileName);
                mOkHttpManager.postAsyncForm(NetworkApi.UPLOAD_TASK_IMG, map, new OkHttpManager.DataCallback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(String json) {
                        Toast.makeText(UploadPictureService.this, "上传成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            // 清空数据库
            photoBeanDao.deleteAll();
            stopSelf();
        }
    }

}

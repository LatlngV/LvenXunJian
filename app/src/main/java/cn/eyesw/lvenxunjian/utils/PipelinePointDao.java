package cn.eyesw.lvenxunjian.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import cn.eyesw.lvenxunjian.bean.PipelinePointBean;
import cn.eyesw.lvenxunjian.bean.StaffPointBean;
import cn.eyesw.lvenxunjian.constant.Constant;
import cn.eyesw.lvenxunjian.database.DBHelper;

public class PipelinePointDao {

    private DBHelper mDBHelper;

    public PipelinePointDao(Context context) {
        mDBHelper = new DBHelper(context);
    }

    /**
     * 增
     */
    public void add(PipelinePointBean pipelinePointBean) {
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("latitude", pipelinePointBean.getLatitude());
        values.put("longitude", pipelinePointBean.getLongitude());
        database.insert(Constant.PIPELINE_POINT_TABLE, null, values);
        database.close();
    }

    /**
     * 删
     */
    public void delete() {
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        database.delete(Constant.PIPELINE_POINT_TABLE, null, null);
        database.close();
    }

    /**
     * 改
     */
    public void update() {
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        /* 需要更新相应数据 */
        database.close();
    }

    /**
     * 查
     */
    public List<PipelinePointBean> select() {
        List<PipelinePointBean> list = new ArrayList<>();
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        Cursor cursor = database.query(Constant.PIPELINE_POINT_TABLE, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            double latitude = cursor.getDouble(0);
            double longitude = cursor.getDouble(1);
            PipelinePointBean pipelinePointBean = new PipelinePointBean(latitude, longitude);
            list.add(pipelinePointBean);
        }
        cursor.close();
        database.close();
        return list;
    }

}

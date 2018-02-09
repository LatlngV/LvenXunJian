package cn.eyesw.lvenxunjian.bean;

import java.io.Serializable;

/**
 * 巡检人员
 */
public class StaffEntity implements Serializable{

    private String mId;
    private String mName;

    public StaffEntity(String id, String name) {
        mId = id;
        mName = name;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    @Override
    public String toString() {
        return ", staffName == " + mName;
    }
}

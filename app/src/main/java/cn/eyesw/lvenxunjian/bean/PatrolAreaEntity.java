package cn.eyesw.lvenxunjian.bean;

import java.io.Serializable;
import java.util.List;

/**
 * You may think you know what the following code does.
 * But you dont. Trust me.
 * Fiddle with it, and youll spend many a sleepless
 * night cursing the moment you thought youd be clever
 * enough to "optimize" the code below.
 * Now close this file and go play with something else.
 */

public class PatrolAreaEntity implements Serializable {

    private String mId;
    private String mName;
    private List<StaffEntity> mList;

    public PatrolAreaEntity(String id, String name, List<StaffEntity> list) {
        mId = id;
        mName = name;
        mList = list;
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

    public List<StaffEntity> getList() {
        return mList;
    }

    public void setList(List<StaffEntity> list) {
        mList = list;
    }
}

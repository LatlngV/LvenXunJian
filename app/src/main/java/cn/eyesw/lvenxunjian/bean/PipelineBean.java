package cn.eyesw.lvenxunjian.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

import cn.eyesw.greendao.DaoSession;
import cn.eyesw.greendao.PipelinePointBeanDao;
import cn.eyesw.greendao.PipelineBeanDao;

@Entity
public class PipelineBean {

    @Id
    private long id;
    @ToMany(referencedJoinProperty = "pipelineId")
    private List<PipelinePointBean> pipelinePointBeanList;
    private String name;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 705841255)
    private transient PipelineBeanDao myDao;

    @Generated(hash = 715219834)
    public PipelineBean(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Generated(hash = 1056299732)
    public PipelineBean() {
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 238104782)
    public List<PipelinePointBean> getPipelinePointBeanList() {
        if (pipelinePointBeanList == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PipelinePointBeanDao targetDao = daoSession.getPipelinePointBeanDao();
            List<PipelinePointBean> pipelinePointBeanListNew = targetDao
                    ._queryPipelineBean_PipelinePointBeanList(id);
            synchronized (this) {
                if (pipelinePointBeanList == null) {
                    pipelinePointBeanList = pipelinePointBeanListNew;
                }
            }
        }
        return pipelinePointBeanList;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 968594309)
    public synchronized void resetPipelinePointBeanList() {
        pipelinePointBeanList = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1502417306)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getPipelineBeanDao() : null;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

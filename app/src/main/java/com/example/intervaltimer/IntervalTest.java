package com.example.intervaltimer;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.Property;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.DaoException;

@Entity
public class IntervalTest implements Comparable<IntervalTest> {

    @Id
    private Long id;

    @Index(unique = true)
    private String name;

    private long uid;

    @Property
    private Date date;

    @ToMany(referencedJoinProperty = "number")
    @OrderBy("place")
    private List<Interval> tests;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 2132736868)
    private transient IntervalTestDao myDao;

    @Generated(hash = 1075466004)
    public IntervalTest(Long id, String name, long uid, Date date) {
        this.id = id;
        this.name = name;
        this.uid = uid;
        this.date = date;
    }

    public void addInterval(Interval i) {
        this.tests.add(i);
    }

    @Generated(hash = 1519184660)
    public IntervalTest() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 2105005537)
    public List<Interval> getTests() {
        if (tests == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            IntervalDao targetDao = daoSession.getIntervalDao();
            List<Interval> testsNew = targetDao._queryIntervalTest_Tests(id);
            synchronized (this) {
                if (tests == null) {
                    tests = testsNew;
                }
            }
        }
        return tests;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 161596209)
    public synchronized void resetTests() {
        tests = null;
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

    public void setUid(long uid) {
        this.uid = uid;
    }
    public long getUid() {
        return this.uid;
    }

    @Override
    public int compareTo(IntervalTest o) {
        return o.getDate().compareTo(this.getDate());
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 459653130)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getIntervalTestDao() : null;
    }
}
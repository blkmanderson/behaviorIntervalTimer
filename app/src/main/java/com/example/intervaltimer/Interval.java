package com.example.intervaltimer;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.NotNull;

@Entity
public class Interval implements Comparable<Interval> {

    @Id
    private Long id;

    @Property
    private int type;

    @Property
    private long length;

    private long number;

    private Integer place;

    @ToOne(joinProperty = "number")
    private IntervalTest test;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1300194505)
    private transient IntervalDao myDao;

    @Generated(hash = 1138612962)
    private transient Long test__resolvedKey;

    @Override
    public String toString() {
        return this.place.toString() + ": " + this.getLength();
    }

    @Generated(hash = 1888777804)
    public Interval(Long id, int type, long length, long number, Integer place) {
        this.id = id;
        this.type = type;
        this.length = length;
        this.number = number;
        this.place = place;
    }

    @Override
    public int compareTo(Interval o) {
        return this.getPlace().compareTo(o.getPlace());
    }

    @Generated(hash = 17763978)
    public Interval() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getLength() {
        return this.length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getNumber() {
        return this.number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 878811689)
    public IntervalTest getTest() {
        long __key = this.number;
        if (test__resolvedKey == null || !test__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            IntervalTestDao targetDao = daoSession.getIntervalTestDao();
            IntervalTest testNew = targetDao.load(__key);
            synchronized (this) {
                test = testNew;
                test__resolvedKey = __key;
            }
        }
        return test;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 2038307716)
    public void setTest(@NotNull IntervalTest test) {
        if (test == null) {
            throw new DaoException(
                    "To-one property 'number' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.test = test;
            number = test.getId();
            test__resolvedKey = number;
        }
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

    public void setPlace(Integer place) {
        this.place = place;
    }
    public Integer getPlace() {
        return this.place;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 358103574)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getIntervalDao() : null;
    }
}

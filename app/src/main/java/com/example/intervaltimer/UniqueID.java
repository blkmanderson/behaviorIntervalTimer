package com.example.intervaltimer;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class UniqueID {

    @Id
    private Long id;

    @Property
    private Long unique;

    UniqueID() {
    }

    @Generated(hash = 1438429938)
    public UniqueID(Long id, Long unique) {
        this.id = id;
        this.unique = unique;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUnique() {
        return this.unique;
    }

    public void setUnique(Long unique) {
        this.unique = unique;
    }
}

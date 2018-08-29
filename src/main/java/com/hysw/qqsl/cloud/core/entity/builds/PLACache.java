package com.hysw.qqsl.cloud.core.entity.builds;

import java.util.Date;

/**
 * @author Administrator
 * @since 2018/8/20
 */
public class PLACache {
    private Date date;
    private Object object;

    public PLACache(Date date, Object object) {
        this.date = date;
        this.object = object;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}

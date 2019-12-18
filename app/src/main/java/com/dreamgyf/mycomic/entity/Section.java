package com.dreamgyf.mycomic.entity;

import java.io.Serializable;

public class Section implements Serializable {

    private String name;

    private String href;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}

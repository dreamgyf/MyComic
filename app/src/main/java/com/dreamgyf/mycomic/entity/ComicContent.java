package com.dreamgyf.mycomic.entity;

import java.io.Serializable;

public class ComicContent implements Serializable {

    private String img;

    private String img_webp;

    private int p;

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getImg_webp() {
        return img_webp;
    }

    public void setImg_webp(String img_webp) {
        this.img_webp = img_webp;
    }

    public int getP() {
        return p;
    }

    public void setP(int p) {
        this.p = p;
    }
}

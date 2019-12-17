package com.dreamgyf.mycomic.entity;

import android.graphics.Bitmap;

import java.io.Serializable;

public class SearchResultEntity implements Serializable {

    private String href;

    private Bitmap image;

    private String title;

    private String author;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}

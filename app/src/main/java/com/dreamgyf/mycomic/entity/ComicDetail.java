package com.dreamgyf.mycomic.entity;

import java.io.Serializable;
import java.util.List;

public class ComicDetail implements Serializable {

    private String id;

    private String title;

    private String author;

    private String description;

    private List<ComicTab> tabs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ComicTab> getTabs() {
        return tabs;
    }

    public void setTabs(List<ComicTab> tabs) {
        this.tabs = tabs;
    }
}

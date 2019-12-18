package com.dreamgyf.mycomic.entity;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class ComicTab implements Serializable {

    private String id;

    private String name;

    private LinkedList<Section> sections;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LinkedList<Section> getSections() {
        return sections;
    }

    public void setSections(LinkedList<Section> sections) {
        this.sections = sections;
    }
}

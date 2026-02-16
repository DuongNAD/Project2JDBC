package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class Section {
    private int sectionId;
    private String title;
    private List<String> lessons = new ArrayList<>();

    public Section() {}

    public Section(int sectionId, String title) {
        this.sectionId = sectionId;
        this.title = title;
    }

    public void addLesson(String lessonName) {
        this.lessons.add(lessonName);
    }

    public List<String> getLessons() { return lessons; }

    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

}
package org.example.model;

import java.sql.Timestamp;

public class MyCourses extends Course {
    private Timestamp enrolledAt;
    private int progress;

    public MyCourses() {
        super();
    }

    public MyCourses(int courseId, String title, String description, double price, String imageUrl, Timestamp enrolledAt) {
        super();
        this.setCourseId(courseId);
        this.setTitle(title);
        this.setDescription(description);
        this.setPrice(price);
        this.setThumbnailUrl(imageUrl);
        this.enrolledAt = enrolledAt;
    }

    public Timestamp getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(Timestamp enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
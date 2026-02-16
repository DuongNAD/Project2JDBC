package org.example.model;

import java.sql.Timestamp;

public class Article {
    private int id; // Đặt tên là id cho gọn, khớp với DAO
    private String title;
    private String description;
    private String content;
    private String imageUrl;
    private String tags;
    private Timestamp createdAt;

    public Article() {}

    public Article(int id, String title, String description, String content, String imageUrl, String tags) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.imageUrl = imageUrl;
        this.tags = tags;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }
    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getTags() { return tags; }
    public Timestamp getCreatedAt() { return createdAt; }
    public String getContent() { return content; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setTags(String tags) { this.tags = tags; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setContent(String content) { this.content = content; }
}
package org.example.model;

public class Course {
    private int courseId;
    private int categoryId;
    private String title;
    private String subtitle;
    private String description;
    private double price;
    private double salePrice;
    private String thumbnailUrl;
    private String level;
    private String status;
    private String categoryName;
    private int progressPercent;

    public Course() {
    }

    public Course(int courseId, int categoryId, String title, String subtitle, String description, double price, double salePrice, String thumbnailUrl, String level, String status) {
        this.courseId = courseId;
        this.categoryId = categoryId;
        this.title = title;
        this.subtitle = subtitle;
        this.description = description;
        this.price = price;
        this.salePrice = salePrice;
        this.thumbnailUrl = thumbnailUrl;
        this.level = level;
        this.status = status;
    }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getSalePrice() { return salePrice; }
    public void setSalePrice(double salePrice) { this.salePrice = salePrice; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getProgressPercent() { return progressPercent; }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }
}
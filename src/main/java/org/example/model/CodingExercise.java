package org.example.model;

public class CodingExercise {
    private int id;
    private int courseId;
    private String title;
    private String description;
    private String starterCode;
    private String expectedOutput;
    private String language;

    public CodingExercise() {}

    public CodingExercise(int id, int courseId, String title, String description, String starterCode, String expectedOutput, String language) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.starterCode = starterCode;
        this.expectedOutput = expectedOutput;
        this.language = language;
    }

    public CodingExercise(int id, int courseId, String title, String description, String starterCode, String expectedOutput) {
        this(id, courseId, title, description, starterCode, expectedOutput, "java");
    }

    public int getId() { return id; }

    public int getCourseId() { return courseId; }

    public String getTitle() { return title; }

    public String getDescription() { return description; }

    public String getStarterCode() { return starterCode; }

    public String getExpectedOutput() { return expectedOutput; }

    public String getLanguage() { return language; }

    public void setId(int id) { this.id = id; }

    public void setCourseId(int courseId) { this.courseId = courseId; }

    public void setTitle(String title) { this.title = title; }

    public void setDescription(String description) { this.description = description; }

    public void setStarterCode(String starterCode) { this.starterCode = starterCode; }

    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }

    public void setLanguage(String language) { this.language = language; }
}
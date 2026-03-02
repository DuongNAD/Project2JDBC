package org.example.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.dao.CourseDao;
import org.example.model.Course;
import org.example.util.ThemeManager;
import org.example.util.UserSession;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MyCoursesController implements Initializable {
    @FXML private FlowPane myCoursesContainer;
    @FXML private HeaderController headerController;

    private CourseDao courseDao = new CourseDao();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        
        if (headerController != null) {
            headerController.setTitle("Khóa học của tôi");
            headerController.showThemeButton(true);
        }

        
        loadData();

        
        
        if (myCoursesContainer.getScene() != null) {
            
            org.example.util.ThemeManager.applyTheme(myCoursesContainer.getScene().getRoot());
        } else {
            
            myCoursesContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    
                    javafx.application.Platform.runLater(() ->
                            org.example.util.ThemeManager.applyTheme(newScene.getRoot())
                    );
                }
            });
        }
    }

    private void loadData() {
        int userId = UserSession.getInstance().getUser().getId();
        List<Course> courses = courseDao.getMyCourses(userId);

        myCoursesContainer.getChildren().clear();
        if (courses.isEmpty()) {
            Label emptyLbl = new Label("Bạn chưa đăng ký khóa học nào.");
            emptyLbl.getStyleClass().add("text-description");
            emptyLbl.setStyle("-fx-font-size: 16px; -fx-padding: 20;");
            myCoursesContainer.getChildren().add(emptyLbl);
        } else {
            for (Course c : courses) {
                VBox card = createCard(c);
                myCoursesContainer.getChildren().add(card);
            }
        }
    }

    private VBox createCard(Course c) {
        
        VBox card = new VBox(10);
        card.setPrefWidth(260);

        
        card.getStyleClass().add("my-course-card");

        
        ImageView img = new ImageView();
        try {
            if (c.getThumbnailUrl() != null && !c.getThumbnailUrl().isEmpty()) {
                img.setImage(new Image(c.getThumbnailUrl(), 260, 145, false, true));
            } else {
                img.setImage(new Image(getClass().getResource("/Images/default.png").toExternalForm()));
            }
        } catch (Exception e) {
            System.err.println("Lỗi load ảnh: " + e.getMessage());
        }
        img.setFitWidth(260);
        img.setFitHeight(145);

        
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(260, 145);
        clip.setArcWidth(16); 
        clip.setArcHeight(16);
        img.setClip(clip);

        
        VBox content = new VBox(8);
        content.setStyle("-fx-padding: 0 15 15 15;"); 

        
        Label title = new Label(c.getTitle());
        title.setWrapText(true);
        title.setPrefHeight(45);
        
        title.getStyleClass().add("course-title-text");

        
        VBox progressBox = new VBox(5);

        int userId = UserSession.getInstance().getUser().getId();
        int completedLessons = courseDao.getCompletedLessonCount(userId, c.getCourseId());
        int totalLessons = courseDao.getTotalLessonCount(c.getCourseId());
        double calcPercent = (totalLessons > 0) ? ((double) completedLessons / totalLessons) : 0;
        int displayPercent = (int) (calcPercent * 100);

        
        Label progressText = new Label("Tiến độ: " + displayPercent + "% (" + completedLessons + "/" + totalLessons + ")");
        
        progressText.getStyleClass().add("course-progress-text");

        
        ProgressBar progressBar = new ProgressBar(calcPercent);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(8);
        
        progressBar.getStyleClass().add("course-progress-bar");

        progressBox.getChildren().addAll(progressText, progressBar);

        
        Button btnLearn = new Button("Vào học ngay");
        btnLearn.setMaxWidth(Double.MAX_VALUE);
        
        btnLearn.getStyleClass().add("btn-enter-course");

        btnLearn.setOnAction(e -> {
            System.out.println(">>> Đang mở khóa học: " + c.getTitle());
            org.example.util.Navigation.toSync(
                    e,
                    org.example.util.Navigation.LEARNING_VIEW,
                    (org.example.controller.LearningController ctrl) -> {
                        ctrl.setCourseData(c);
                    }
            );
        });

        content.getChildren().addAll(title, progressBox, btnLearn);
        card.getChildren().addAll(img, content);

        return card;
    }
}
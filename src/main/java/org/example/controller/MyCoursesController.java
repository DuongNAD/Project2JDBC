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
        // 1. Setup Header
        if (headerController != null) {
            headerController.setTitle("Khóa học của tôi");
            headerController.showThemeButton(true);
        }

        // 2. Load Dữ liệu (Để lên trên cho gọn)
        loadData();

        // 3. --- ĐOẠN CODE FIX LỖI THEME (QUAN TRỌNG) ---
        // Kiểm tra xem giao diện đã được gắn vào Scene chưa
        if (myCoursesContainer.getScene() != null) {
            // Trường hợp 1: Đã có Scene (thường gặp khi load lại trang) -> Apply ngay
            org.example.util.ThemeManager.applyTheme(myCoursesContainer.getScene().getRoot());
        } else {
            // Trường hợp 2: Chưa có Scene (lần đầu mở) -> Lắng nghe sự kiện
            myCoursesContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    // Khi Scene xuất hiện, dùng Platform.runLater để đảm bảo UI đã vẽ xong rồi mới đổi màu
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
        // 1. Tạo khung thẻ (Card)
        VBox card = new VBox(10);
        card.setPrefWidth(260);

        // --- SỬA: Dùng đúng class CSS, xóa style cứng ---
        card.getStyleClass().add("my-course-card");

        // 2. Ảnh khóa học
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

        // Bo góc ảnh (Giữ nguyên logic này vì CSS khó bo ảnh trực tiếp trong JavaFX cũ)
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(260, 145);
        clip.setArcWidth(16); // Khớp với radius của CSS
        clip.setArcHeight(16);
        img.setClip(clip);

        // 3. Nội dung thẻ
        VBox content = new VBox(8);
        content.setStyle("-fx-padding: 0 15 15 15;"); // Padding nội bộ

        // Tiêu đề
        Label title = new Label(c.getTitle());
        title.setWrapText(true);
        title.setPrefHeight(45);
        // --- SỬA: Dùng class CSS chuẩn ---
        title.getStyleClass().add("course-title-text");

        // --- TÍNH TIẾN ĐỘ ---
        VBox progressBox = new VBox(5);

        int userId = UserSession.getInstance().getUser().getId();
        int completedLessons = courseDao.getCompletedLessonCount(userId, c.getCourseId());
        int totalLessons = courseDao.getTotalLessonCount(c.getCourseId());
        double calcPercent = (totalLessons > 0) ? ((double) completedLessons / totalLessons) : 0;
        int displayPercent = (int) (calcPercent * 100);

        // Text tiến độ
        Label progressText = new Label("Tiến độ: " + displayPercent + "% (" + completedLessons + "/" + totalLessons + ")");
        // --- SỬA: Dùng class CSS chuẩn ---
        progressText.getStyleClass().add("course-progress-text");

        // Thanh ProgressBar
        ProgressBar progressBar = new ProgressBar(calcPercent);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(8);
        // --- SỬA: Dùng class CSS chuẩn ---
        progressBar.getStyleClass().add("course-progress-bar");

        progressBox.getChildren().addAll(progressText, progressBar);

        // Nút Vào học
        Button btnLearn = new Button("Vào học ngay");
        btnLearn.setMaxWidth(Double.MAX_VALUE);
        // --- SỬA: Dùng class CSS chuẩn ---
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
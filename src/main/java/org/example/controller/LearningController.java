package org.example.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.example.dao.CourseDao;
import org.example.model.CodingExercise;
import org.example.model.Course;
import org.example.model.Section;
import org.example.util.UserSession;
import java.util.Optional;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class LearningController implements Initializable {

    @FXML
    private Label lblCourseTitle;
    @FXML
    private WebView videoPlayer;
    @FXML
    private VBox curriculumContainer;
    @FXML
    private Label lblCurrentLesson;

    @FXML
    private Button btnPrev;
    @FXML
    private Button btnNext;

    @FXML
    private Button star1;
    @FXML
    private Button star2;
    @FXML
    private Button star3;
    @FXML
    private Button star4;
    @FXML
    private Button star5;

    private Course currentCourse;
    private CourseDao courseDao = new CourseDao();
    private org.example.dao.ReviewDao reviewDao = new org.example.dao.ReviewDao();
    private WebEngine webEngine;

    private final String DEMO_VIDEO_URL = "https://www.youtube.com/embed/grEKMHGYyns?autoplay=1&rel=0";

    private static class LessonItem {
        String title;
        String type;
        Object data;

        public LessonItem(String title, String type, Object data) {
            this.title = title;
            this.type = type;
            this.data = data;
        }
    }

    private List<LessonItem> allLessons = new ArrayList<>();
    private int currentIndex = -1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (videoPlayer != null) {
            webEngine = videoPlayer.getEngine();
            webEngine.setJavaScriptEnabled(true);
        }
    }

    public void setCourseData(Course course) {
        this.currentCourse = course;
        if (lblCourseTitle != null) {
            lblCourseTitle.setText("Đang học: " + course.getTitle());
        }

        loadCurriculum(course.getCourseId());

        if (!allLessons.isEmpty()) {
            currentIndex = 0;

            playLessonAtIndex(currentIndex, null);
        }

        loadUserRating();
    }

    private void loadUserRating() {
        if (UserSession.getInstance().getUser() == null || currentCourse == null)
            return;
        int userId = UserSession.getInstance().getUser().getId();
        int rating = courseDao.getCourseRating(userId, currentCourse.getCourseId());
        updateStarsUI(rating);
    }

    private void loadCurriculum(int courseId) {
        if (curriculumContainer == null)
            return;

        allLessons.clear();
        curriculumContainer.getChildren().clear();

        List<Section> sections = courseDao.getCurriculum(courseId);

        for (Section sec : sections) {
            Label secTitle = new Label(sec.getTitle());
            secTitle.setStyle(
                    "-fx-font-weight: bold; -fx-padding: 10 15; -fx-background-color: #E2E8F0; -fx-text-fill: #1E293B;");
            secTitle.setMaxWidth(Double.MAX_VALUE);
            curriculumContainer.getChildren().add(secTitle);

            for (String lessonName : sec.getLessons()) {
                allLessons.add(new LessonItem(lessonName, "VIDEO", lessonName));
                int myIndex = allLessons.size() - 1;
                HBox row = createLessonRow("▶", lessonName, myIndex);
                curriculumContainer.getChildren().add(row);
            }
        }

        List<CodingExercise> exercises = courseDao.getExercises(courseId);
        if (!exercises.isEmpty()) {
            Label exeHeader = new Label("⚡ Bài tập thực hành (Coding)");
            exeHeader.setStyle(
                    "-fx-font-weight: bold; -fx-padding: 15; -fx-background-color: #3B82F6; -fx-text-fill: white;");
            exeHeader.setMaxWidth(Double.MAX_VALUE);
            curriculumContainer.getChildren().add(exeHeader);

            for (CodingExercise exe : exercises) {
                allLessons.add(new LessonItem(exe.getTitle(), "CODE", exe));
                int myIndex = allLessons.size() - 1;
                HBox row = createLessonRow("⌨", exe.getTitle(), myIndex);
                curriculumContainer.getChildren().add(row);
            }
        }
    }

    private HBox createLessonRow(String iconSymbol, String title, int index) {
        HBox row = new HBox(10);
        row.getStyleClass().add("lesson-item");
        row.setStyle("-fx-padding: 10 15; -fx-cursor: hand; -fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0;");

        Label icon = new Label(iconSymbol);
        icon.setStyle("-fx-text-fill: #3B82F6;");
        Label lbl = new Label(title);
        lbl.setWrapText(true);
        lbl.setStyle("-fx-text-fill: #334155;");

        row.getChildren().addAll(icon, lbl);

        row.setOnMouseClicked(e -> {
            currentIndex = index;
            playLessonAtIndex(currentIndex, e);
        });

        return row;
    }

    private void playLessonAtIndex(int index, Event event) {
        if (index < 0 || index >= allLessons.size())
            return;

        LessonItem item = allLessons.get(index);

        if (lblCurrentLesson != null)
            lblCurrentLesson.setText(item.title);

        if (item.type.equals("VIDEO")) {
            if (webEngine != null) {
                webEngine.load(DEMO_VIDEO_URL);
            }
        } else if (item.type.equals("CODE")) {
            if (webEngine != null)
                webEngine.load(null);

            CodingExercise exe = (CodingExercise) item.data;

            openCodingPractice(exe, event);
        }

        if (btnPrev != null)
            btnPrev.setDisable(index == 0);
        if (btnNext != null)
            btnNext.setDisable(index == allLessons.size() - 1);
    }

    @FXML
    void handlePrevLesson(ActionEvent event) {
        if (currentIndex > 0) {
            currentIndex--;
            playLessonAtIndex(currentIndex, event);
        }
    }

    @FXML
    void handleNextLesson(ActionEvent event) {
        markCurrentLessonAsDone();
        if (currentIndex < allLessons.size() - 1) {
            currentIndex++;
            playLessonAtIndex(currentIndex, event);
        }
    }

    private void markCurrentLessonAsDone() {
        if (currentIndex < 0 || currentIndex >= allLessons.size())
            return;
        LessonItem item = allLessons.get(currentIndex);
        int userId = UserSession.getInstance().getUser().getId();
        int courseId = currentCourse.getCourseId();

        new Thread(() -> {
            courseDao.markLessonAsCompleted(userId, courseId, item.title);
            System.out.println("✅ Đã lưu tiến độ: " + item.title);
        }).start();
    }

    private void openCodingPractice(CodingExercise exe, Event event) {
        try {
            if (webEngine != null)
                webEngine.load(null);

            if (event != null) {
                org.example.util.Navigation.toSync(
                        event,
                        org.example.util.Navigation.CODING_VIEW,
                        (org.example.controller.CodingPracticeController controller) -> {
                            controller.setExerciseData(exe);
                        });
            }

            else {
                System.out.println("⚠️ Cảnh báo: Event null, đang thử lấy Stage thủ công...");
                if (lblCourseTitle.getScene() != null) {
                    Stage stage = (Stage) lblCourseTitle.getScene().getWindow();

                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                            getClass().getResource(org.example.util.Navigation.CODING_VIEW));
                    javafx.scene.Parent root = loader.load();

                    org.example.util.ThemeManager.applyTheme(root);

                    javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) stage.getScene()
                            .lookup("#contentArea");
                    if (contentArea != null) {
                        contentArea.getChildren().clear();
                        contentArea.getChildren().add(root);

                        org.example.controller.CodingPracticeController controller = loader.getController();
                        controller.setExerciseData(exe);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi mở bài tập code: " + e.getMessage());
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        if (webEngine != null)
            webEngine.load(null);
        org.example.util.Navigation.to(event, org.example.util.Navigation.MY_COURSES_VIEW);
    }

    @FXML
    void handleRating1(ActionEvent event) {
        openReviewDialog(1);
    }

    @FXML
    void handleRating2(ActionEvent event) {
        openReviewDialog(2);
    }

    @FXML
    void handleRating3(ActionEvent event) {
        openReviewDialog(3);
    }

    @FXML
    void handleRating4(ActionEvent event) {
        openReviewDialog(4);
    }

    @FXML
    void handleRating5(ActionEvent event) {
        openReviewDialog(5);
    }

    private void openReviewDialog(int initialRating) {
        if (UserSession.getInstance().getUser() == null || currentCourse == null)
            return;
        int userId = UserSession.getInstance().getUser().getId();

        javafx.scene.control.Dialog<javafx.scene.control.ButtonType> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Đánh giá khóa học");
        dialog.setHeaderText("Chia sẻ cảm nhận của bạn về khóa học này");

        javafx.scene.control.ButtonType submitBtnType = new javafx.scene.control.ButtonType("Gửi đánh giá",
                javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitBtnType, javafx.scene.control.ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 20; -fx-background-color: transparent;");

        HBox starBox = new HBox(5);
        Label[] starLabels = new Label[5];
        final int[] currentRatingArray = { initialRating };

        for (int i = 0; i < 5; i++) {
            starLabels[i] = new Label("★");
            boolean active = i < initialRating;
            starLabels[i].setStyle("-fx-font-size: 28px; -fx-cursor: hand; "
                    + (active ? "-fx-text-fill: #F6AD55;" : "-fx-text-fill: #CBD5E0;"));
            int starValue = i + 1;

            starLabels[i].setOnMouseClicked(e -> {
                currentRatingArray[0] = starValue;
                for (int j = 0; j < 5; j++) {
                    starLabels[j].setStyle("-fx-font-size: 28px; -fx-cursor: hand; "
                            + (j < starValue ? "-fx-text-fill: #F6AD55;" : "-fx-text-fill: #CBD5E0;"));
                }
            });
            starBox.getChildren().add(starLabels[i]);
        }

        javafx.scene.control.TextArea txtComment = new javafx.scene.control.TextArea();
        txtComment.setPromptText("Viết nhận xét của bạn...");
        txtComment.setPrefRowCount(4);
        txtComment.setWrapText(true);
        txtComment.setStyle("-fx-font-family: inherit; -fx-font-size: 14px;");

        org.example.model.Review existingReview = reviewDao.getReviewByUser(userId, currentCourse.getCourseId());
        if (existingReview != null) {
            currentRatingArray[0] = existingReview.getRating();
            for (int j = 0; j < 5; j++) {
                starLabels[j].setStyle("-fx-font-size: 28px; -fx-cursor: hand; "
                        + (j < currentRatingArray[0] ? "-fx-text-fill: #F6AD55;" : "-fx-text-fill: #CBD5E0;"));
            }
            txtComment.setText(existingReview.getComment() != null ? existingReview.getComment() : "");
        }

        content.getChildren().addAll(new Label("Xếp hạng:"), starBox, txtComment);
        dialog.getDialogPane().setContent(content);

        // Styling the dialog
        org.example.util.ThemeManager.applyTheme(dialog.getDialogPane());

        Optional<javafx.scene.control.ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == submitBtnType) {
            boolean success = reviewDao.submitReview(userId, currentCourse.getCourseId(), currentRatingArray[0],
                    txtComment.getText());
            if (success) {
                updateStarsUI(currentRatingArray[0]);
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Cảm ơn bạn đã đánh giá khóa học!");
                alert.showAndWait();
            } else {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setContentText("Có lỗi xảy ra khi gửi đánh giá.");
                alert.showAndWait();
            }
        }
    }

    private void updateStarsUI(int rating) {
        Button[] stars = { star1, star2, star3, star4, star5 };
        for (int i = 0; i < stars.length; i++) {
            if (stars[i] != null) {
                if (i < rating) {
                    stars[i].setStyle(
                            "-fx-background-color: transparent; -fx-font-size: 20px; -fx-text-fill: #F59E0B; -fx-cursor: hand;");
                } else {
                    stars[i].setStyle(
                            "-fx-background-color: transparent; -fx-font-size: 20px; -fx-text-fill: gray; -fx-cursor: hand;");
                }
            }
        }
    }
}
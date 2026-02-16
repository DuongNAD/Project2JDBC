package org.example.controller;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.dao.CourseDao;
import org.example.model.Course;
import org.example.model.Section;
import org.example.util.ScrollUtil;
import org.example.util.ThemeManager;
import org.example.util.UserSession;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CourseDetailController implements Initializable {

    @FXML private ImageView imgThumbnail;
    @FXML private Label lblCategory;
    @FXML private Label lblTitle;
    @FXML private Label lblInstructor;
    @FXML private Label lblPrice;
    @FXML private Text txtDescription;
    @FXML private Button btnEnroll;
    @FXML private VBox curriculumContainer;
    @FXML private ScrollPane mainScrollPane;

    private CourseDao courseDao = new CourseDao();

    // 1. THÊM BIẾN NÀY ĐỂ LƯU KHÓA HỌC HIỆN TẠI
    private Course currentCourse;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ScrollUtil.applySmoothScrolling(mainScrollPane);

        Platform.runLater(() -> {
            if (lblTitle.getScene() != null) {
                ThemeManager.applyTheme(lblTitle.getScene().getRoot());
            }
        });
    }

    public void setCourseData(Course course) {
        if (course == null) return;

        // 2. LƯU DỮ LIỆU VÀO BIẾN TOÀN CỤC
        this.currentCourse = course;

        lblTitle.setText(course.getTitle());
        lblCategory.setText(course.getCategoryName() != null ? course.getCategoryName().toUpperCase() : "CHUNG");

        if (course.getSalePrice() > 0) {
            lblPrice.setText(String.format("%,.0f đ", course.getSalePrice()));
            lblPrice.setStyle("-fx-text-fill: #E53E3E; -fx-font-weight: bold;");
        } else {
            lblPrice.setText(String.format("%,.0f đ", course.getPrice()));
        }

        txtDescription.setText(course.getDescription());

        try {
            if (course.getThumbnailUrl() != null && !course.getThumbnailUrl().isEmpty()) {
                imgThumbnail.setImage(new Image(course.getThumbnailUrl(), 420, 250, false, true));
            }
        } catch (Exception e) {
            // Có thể set ảnh mặc định nếu lỗi
        }

        loadCurriculum(course.getCourseId());
    }

    // 3. THÊM HÀM XỬ LÝ SỰ KIỆN NÚT "ĐĂNG KÝ HỌC NGAY"
    @FXML
    void handleEnroll(ActionEvent event) {
        try {
            // Kiểm tra Login
            if (UserSession.getInstance().getUser() == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Yêu cầu đăng nhập");
                alert.setHeaderText(null);
                alert.setContentText("Vui lòng đăng nhập để đăng ký khóa học này!");
                alert.showAndWait();
                return;
            }

            // Load Popup Thanh toán
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/payment.fxml"));
            Parent root = loader.load();

            // Truyền dữ liệu khóa học sang PaymentController
            PaymentController controller = loader.getController();
            controller.setPaymentData(this.currentCourse);

            // Tạo cửa sổ mới (Stage) dạng Modal (Cửa sổ con)
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); // Chặn không cho bấm cửa sổ chính khi chưa đóng popup
            stage.setTitle("Thanh toán - EduPath");

            // Áp dụng Theme cho Popup để đồng bộ
            ThemeManager.applyTheme(root);

            stage.setScene(new Scene(root));
            stage.showAndWait(); // Chờ người dùng thanh toán xong mới chạy tiếp

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi khi mở trang thanh toán: " + e.getMessage());
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/courses.fxml")); // Hoặc home.fxml tùy logic của anh
            Parent view = loader.load();
            StackPane contentArea = (StackPane) lblTitle.getScene().lookup("#contentArea");

            if (contentArea != null) {
                ThemeManager.applyTheme(view);
                view.setOpacity(0);
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);

                FadeTransition fade = new FadeTransition(Duration.millis(300), view);
                fade.setFromValue(0);
                fade.setToValue(1);
                fade.play();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCurriculum(int courseId) {
        if (curriculumContainer == null) return;
        curriculumContainer.getChildren().clear();

        List<Section> sections = courseDao.getCurriculum(courseId);

        if (sections.isEmpty()) {
            Label emptyLbl = new Label("Chương trình học đang được cập nhật...");
            emptyLbl.setStyle("-fx-text-fill: #718096; -fx-font-style: italic;");
            curriculumContainer.getChildren().add(emptyLbl);
            return;
        }

        Accordion accordion = new Accordion();

        for (Section sec : sections) {
            VBox lessonBox = new VBox(0);

            for (String lesson : sec.getLessons()) {
                HBox row = new HBox(12);
                row.getStyleClass().add("lesson-item");
                Text icon = new Text("▶");
                icon.getStyleClass().add("lesson-icon");
                Label lblLesson = new Label(lesson);
                lblLesson.getStyleClass().add("lesson-title");
                lblLesson.setWrapText(true);
                row.getChildren().addAll(icon, lblLesson);
                lessonBox.getChildren().add(row);
            }

            TitledPane pane = new TitledPane(sec.getTitle(), lessonBox);
            pane.getStyleClass().add("course-section-pane");
            accordion.getPanes().add(pane);
        }

        if (!accordion.getPanes().isEmpty()) {
            accordion.setExpandedPane(accordion.getPanes().get(0));
        }
        curriculumContainer.getChildren().add(accordion);
    }
}
package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.dao.CourseDao;
import org.example.dao.NotificationDao; 
import org.example.model.Course;
import org.example.util.UserSession;

public class PaymentController {

    @FXML private ImageView imgThumbnail;
    @FXML private Label lblCourseTitle;
    @FXML private Label lblPrice;
    @FXML private Button btnCancel;
    @FXML private Button btnConfirm;

    private Course course;
    private CourseDao courseDao = new CourseDao();

    public void setPaymentData(Course course) {
        this.course = course;
        if (course == null) return;

        lblCourseTitle.setText(course.getTitle());
        lblPrice.setText(String.format("%,.0f đ", course.getSalePrice() > 0 ? course.getSalePrice() : course.getPrice()));

        try {
            if (course.getThumbnailUrl() != null && !course.getThumbnailUrl().isEmpty()) {
                imgThumbnail.setImage(new Image(course.getThumbnailUrl(), 140, 80, false, true));
            }
        } catch (Exception e) {}
    }

    @FXML
    public void initialize() {
        btnCancel.setOnAction(e -> closeWindow());
        btnConfirm.setOnAction(e -> handleProcessPayment());
    }

    
    private void handleProcessPayment() {
        if (UserSession.getInstance().getUser() == null) return;

        int userId = UserSession.getInstance().getUser().getId();

        
        boolean success = courseDao.registerCourse(userId, course.getCourseId());

        if (success) {
            
            NotificationDao notiDao = new NotificationDao();
            String title = "Đăng ký thành công";
            String message = "Bạn đã sở hữu khóa học: " + course.getTitle() + ". Hãy bắt đầu học ngay!";

            
            notiDao.createNotification(userId, title, message, "SUCCESS");
            

            showAlert(Alert.AlertType.INFORMATION, "Thanh toán thành công! Chào mừng bạn vào học.");
            closeWindow();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi giao dịch. Bạn có thể đã sở hữu khóa học này.");
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("Thông báo thanh toán");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
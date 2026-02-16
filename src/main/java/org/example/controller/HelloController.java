package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    // Vùng hiển thị nội dung chính (nơi sẽ thay đổi khi bấm menu)
    @FXML
    private StackPane contentArea;

    // Controller của Sidebar (Được JavaFX tự động tiêm vào)
    // QUY TẮC BẮT BUỘC: Tên biến phải là [id của fx:include] + "Controller"
    // Ví dụ bên FXML: <fx:include fx:id="sidebar" ... /> -> biến phải tên là sidebarController
    @FXML
    private SidebarController sidebarController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 1. Kiểm tra và kết nối với Sidebar
        if (sidebarController != null) {
            // Truyền quyền điều khiển vùng contentArea cho Sidebar
            sidebarController.setMainContentArea(contentArea);
            System.out.println(">>> [SUCCESS] Đã kết nối HelloController với SidebarController.");
        } else {
            System.err.println(">>> [ERROR] Không tìm thấy SidebarController! Kiểm tra lại fx:id='sidebar' trong hello-view.fxml");
        }

        // 2. Load màn hình mặc định khi mở ứng dụng (Thường là Cửa hàng hoặc Trang chủ)
        loadDefaultView();
    }

    private void loadDefaultView() {
        try {
            // SỬA Ở ĐÂY: Đổi từ /View/courses.fxml thành /View/home.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/home.fxml"));
            Parent view = loader.load();

            // Xóa nội dung cũ (nếu có) và thêm nội dung mới
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            System.out.println(">>> [SUCCESS] Đã load màn hình mặc định: Trang chủ");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(">>> [ERROR] Không thể load màn hình mặc định (home.fxml).");
        }
    }
}
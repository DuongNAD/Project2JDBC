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

    
    @FXML
    private StackPane contentArea;

    
    
    
    @FXML
    private SidebarController sidebarController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        
        if (sidebarController != null) {
            
            sidebarController.setMainContentArea(contentArea);
            System.out.println(">>> [SUCCESS] Đã kết nối HelloController với SidebarController.");
        } else {
            System.err.println(">>> [ERROR] Không tìm thấy SidebarController! Kiểm tra lại fx:id='sidebar' trong hello-view.fxml");
        }

        
        loadDefaultView();
    }

    private void loadDefaultView() {
        try {
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/home.fxml"));
            Parent view = loader.load();

            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            System.out.println(">>> [SUCCESS] Đã load màn hình mặc định: Trang chủ");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(">>> [ERROR] Không thể load màn hình mặc định (home.fxml).");
        }
    }
}
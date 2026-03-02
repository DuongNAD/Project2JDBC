package org.example.controller;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.dao.NotificationDao;
import org.example.model.Notification;
import org.example.model.User;
import org.example.util.ThemeManager;
import org.example.util.UserSession;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

public class HeaderController implements Initializable {

    @FXML
    private Label lblPageTitle;
    @FXML
    private Label lblUserName;
    @FXML
    private ImageView imgUserAvatar;
    @FXML
    public Button btnThemeToggle;
    @FXML
    public ImageView iconTheme;
    @FXML
    private TextField txtSearch;

    
    @FXML
    private ImageView iconBell; 
    @FXML
    private Label lblUnreadCount; 

    private NotificationDao notiDao = new NotificationDao();
    private ContextMenu notiMenu; 

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        
        User user = UserSession.getInstance().getUser();
        if (user != null) {
            if (lblUserName != null)
                lblUserName.setText(user.getFullname());
            try {
                if (imgUserAvatar != null && user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                    String aUrl = user.getAvatarUrl();
                    if (aUrl.startsWith("/userAvatar/")) {
                        aUrl = new java.io.File("src/main/resources" + aUrl).toURI().toString();
                    }
                    imgUserAvatar.setImage(new Image(aUrl, 36, 36, true, true));
                }
            } catch (Exception e) {
            }
        }

        updateThemeIcon();

        if (btnThemeToggle != null) {
            btnThemeToggle.setOnAction(e -> toggleTheme());
        }

        
        if (iconBell != null) {
            
            notiMenu = new ContextMenu();
            notiMenu.getStyleClass().add("notification-context-menu"); 

            
            notiMenu.setOnAction(e -> {
            }); 
            notiMenu.getScene().setOnMouseExited(e -> notiMenu.hide()); 
                                                                        

            
            iconBell.setOnMouseEntered(event -> handleShowNotifications());

            
            startNotiPolling();
        }
    }

    
    private void startNotiPolling() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> updateUnreadCount()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        updateUnreadCount(); 
    }

    private void updateUnreadCount() {
        User user = UserSession.getInstance().getUser();
        if (user == null)
            return;

        new Thread(() -> {
            int count = notiDao.countUnread(user.getId());
            Platform.runLater(() -> {
                if (lblUnreadCount != null) {
                    if (count > 0) {
                        lblUnreadCount.setVisible(true);
                        lblUnreadCount.setText(count > 9 ? "9+" : String.valueOf(count));
                    } else {
                        lblUnreadCount.setVisible(false);
                    }
                }
            });
        }).start();
    }

    
    private void handleShowNotifications() {
        User user = UserSession.getInstance().getUser();
        if (user == null)
            return;

        
        notiMenu.getItems().clear();

        
        VBox container = new VBox();
        container.getStyleClass().add("notification-container"); 

        
        HBox header = new HBox();
        header.getStyleClass().add("notification-header");

        Label lblHeader = new Label("Thông báo mới");
        lblHeader.getStyleClass().add("notification-header-label");
        header.getChildren().add(lblHeader);

        
        VBox listItems = new VBox(); 

        List<Notification> list = notiDao.getMyNotifications(user.getId());

        if (list.isEmpty()) {
            Label emptyLbl = new Label("Bạn chưa có thông báo nào.");
            emptyLbl.setStyle("-fx-padding: 20; -fx-text-fill: #94A3B8; -fx-font-style: italic;");
            
            HBox emptyBox = new HBox(emptyLbl);
            emptyBox.setAlignment(javafx.geometry.Pos.CENTER);
            listItems.getChildren().add(emptyBox);
        } else {
            for (Notification n : list) {
                
                listItems.getChildren().add(createNotificationItemRow(n));
            }
        }

        
        HBox footer = new HBox();
        footer.getStyleClass().add("notification-footer");

        Button btnMarkRead = new Button("Đánh dấu tất cả là đã đọc");
        btnMarkRead.getStyleClass().add("btn-mark-read");
        
        btnMarkRead.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        
        btnMarkRead.setOnAction(e -> {
            new Thread(() -> {
                notiDao.markAsRead(user.getId());
                Platform.runLater(() -> {
                    updateUnreadCount();
                    notiMenu.hide(); 
                });
            }).start();
        });

        footer.getChildren().add(btnMarkRead);

        
        container.getChildren().addAll(header, listItems, footer);

        
        ThemeManager.applyTheme(container);

        
        CustomMenuItem item = new CustomMenuItem(container);
        
        item.setHideOnClick(false);

        
        container.setOnMouseExited(e -> {
            notiMenu.hide();
        });

        notiMenu.getItems().add(item);

        
        
        notiMenu.show(iconBell, Side.BOTTOM, -200, 15);
    }

    
    
    private HBox createNotificationItemRow(Notification n) {
        HBox row = new HBox(12);
        row.getStyleClass().add("notification-item"); 

        
        String iconText = "ℹ️"; 
        if ("SUCCESS".equals(n.getType()))
            iconText = "✅";
        else if ("WARNING".equals(n.getType()))
            iconText = "⚠️";

        Label lblIcon = new Label(iconText);
        lblIcon.setStyle("-fx-font-size: 18px;"); 

        VBox iconBox = new VBox(lblIcon);
        iconBox.getStyleClass().add("notif-icon-box"); 

        
        VBox content = new VBox(2);

        Label lblTitle = new Label(n.getTitle());
        lblTitle.getStyleClass().add("notif-title"); 

        Label lblMsg = new Label(n.getMessage());
        lblMsg.getStyleClass().add("notif-msg"); 
        lblMsg.setWrapText(true);
        lblMsg.setMaxWidth(260); 

        
        String timeStr = (n.getCreatedAt() != null)
                ? new SimpleDateFormat("HH:mm dd/MM").format(n.getCreatedAt())
                : "";
        Label lblTime = new Label(timeStr);
        lblTime.getStyleClass().add("notif-time"); 

        content.getChildren().addAll(lblTitle, lblMsg, lblTime);

        
        row.getChildren().addAll(iconBox, content);

        return row;
    }

    
    public void setTitle(String title) {
        if (lblPageTitle != null)
            lblPageTitle.setText(title);
    }

    public void showThemeButton(boolean show) {
        if (btnThemeToggle != null) {
            btnThemeToggle.setVisible(show);
            btnThemeToggle.setManaged(show);
        }
    }

    private void toggleTheme() {
        if (lblPageTitle == null || lblPageTitle.getScene() == null)
            return;
        Parent root = lblPageTitle.getScene().getRoot();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.1);

        fadeOut.setOnFinished(event -> {
            ThemeManager.setDarkMode(!ThemeManager.isDarkMode());
            ThemeManager.applyTheme(root);
            updateThemeIcon();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), root);
            fadeIn.setFromValue(0.1);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    private void updateThemeIcon() {
        if (iconTheme != null) {
            String iconUrl = ThemeManager.isDarkMode()
                    ? "https://img.icons8.com/ios-glyphs/30/ffffff/sun--v1.png"
                    : "https://img.icons8.com/ios-glyphs/30/000000/moon-symbol.png";
            try {
                iconTheme.setImage(new Image(iconUrl));
            } catch (Exception e) {
            }
        }

        if (iconBell != null) {
            String bellUrl = ThemeManager.isDarkMode()
                    ? "https://img.icons8.com/ios-filled/50/ffffff/bell.png"
                    : "https://img.icons8.com/ios-filled/50/334155/bell.png";
            try {
                iconBell.setImage(new Image(bellUrl));
            } catch (Exception e) {
            }
        }
    }

    @FXML
    public void handleSearch(javafx.event.ActionEvent event) {
        if (txtSearch == null)
            return;
        String query = txtSearch.getText().trim();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/courses.fxml"));
            Parent view = loader.load();
            CoursesController controller = loader.getController();

            
            if (!query.isEmpty()) {
                controller.searchCourses(query);
            }

            if (txtSearch.getScene() != null) {
                javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) txtSearch.getScene()
                        .lookup("#contentArea");
                if (contentArea != null) {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(view);
                    ThemeManager.applyTheme(view);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
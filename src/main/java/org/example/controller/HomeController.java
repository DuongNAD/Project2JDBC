package org.example.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.example.dao.HomeDao;
import org.example.model.Article;
import org.example.model.ChatMessage;
import org.example.model.User;
import org.example.util.ThemeManager;
import org.example.util.UserSession;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML
    private Label lblWelcome;
    @FXML
    private FlowPane newsContainer;
    @FXML
    private VBox chatContainer;
    @FXML
    private TextField txtChatInput;
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private Label lblOnlineCount;
    @FXML
    private ScrollPane mainScrollPane;

    
    @FXML
    private HeaderController headerController;

    private HomeDao homeDao = new HomeDao();
    private Timeline chatUpdater;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        
        setupHeaderAndTheme();
        setupUserGreeting();
        loadArticles();
        loadOnlineCount();
        loadChatMessages();
        startChatPolling();
        org.example.util.ScrollUtil.applySmoothScrolling(mainScrollPane);

        
        
        if (mainScrollPane.getScene() != null) {
            
            ThemeManager.applyTheme(mainScrollPane.getScene().getRoot());
        } else {
            
            
            mainScrollPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    
                    Platform.runLater(() -> ThemeManager.applyTheme(newScene.getRoot()));
                }
            });
        }
    }

    private void setupHeaderAndTheme() {
        if (headerController != null) {
            headerController.setTitle("Trang chủ EduPath");
            headerController.showThemeButton(true);
        }
        
        Platform.runLater(() -> {
            if (lblWelcome.getScene() != null) {
                ThemeManager.applyTheme(lblWelcome.getScene().getRoot());
            }
        });
    }

    private void setupUserGreeting() {
        User user = UserSession.getInstance().getUser();
        if (user != null && user.getFullname() != null) {
            String fullName = user.getFullname().trim();
            
            String[] nameParts = fullName.split(" ");
            String ten = nameParts[nameParts.length - 1];
            lblWelcome.setText("Xin chào, " + ten + "!");
        } else {
            lblWelcome.setText("Xin chào, Bạn!");
        }
    }

    
    private void loadArticles() {
        new Thread(() -> {
            List<Article> articles = homeDao.getTopArticles();

            Platform.runLater(() -> {
                newsContainer.getChildren().clear();
                for (Article a : articles) {
                    VBox card = createNewsCard(a);
                    newsContainer.getChildren().add(card);
                }
            });
        }).start();
    }

    private VBox createNewsCard(Article a) {
        VBox card = new VBox();
        card.getStyleClass().add("news-card-vertical");
        card.setSpacing(8);
        card.setPrefWidth(300);

        
        card.setCache(true);
        card.setCacheHint(javafx.scene.CacheHint.SPEED);

        
        ImageView img = new ImageView();
        try {
            String url = (a.getImageUrl() != null && !a.getImageUrl().isEmpty())
                    ? a.getImageUrl()
                    : "https://via.placeholder.com/350x180";
            img.setImage(new Image(url, 350, 180, false, true, true)); 
        } catch (Exception e) {
        }

        img.setFitHeight(180);
        img.setFitWidth(350);
        img.getStyleClass().add("news-img-top");

        
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(350, 180);
        clip.setArcWidth(12);
        clip.setArcHeight(12);
        img.setClip(clip);

        
        VBox content = new VBox(5);
        content.setStyle("-fx-padding: 15;");

        Label title = new Label(a.getTitle());
        title.getStyleClass().add("news-title");
        title.setWrapText(true);
        title.setPrefHeight(45);

        Label desc = new Label(a.getDescription());
        desc.getStyleClass().add("news-desc");
        desc.setWrapText(true);
        VBox.setVgrow(desc, Priority.ALWAYS);

        Label meta = new Label((a.getTags() != null ? a.getTags() : "#News") + " • " + a.getCreatedAt());
        meta.getStyleClass().add("news-meta");

        content.getChildren().addAll(title, desc, meta);
        card.getChildren().addAll(img, content);
        card.setOnMouseClicked(e -> openArticleDetail(a));
        return card;
    }

    
    private void startChatPolling() {
        if (chatUpdater != null)
            chatUpdater.stop();

        chatUpdater = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            loadChatMessages();
            loadOnlineCount();
        }));
        chatUpdater.setCycleCount(Timeline.INDEFINITE);
        chatUpdater.play();
    }

    private void loadChatMessages() {
        User currentUser = UserSession.getInstance().getUser();
        if (currentUser == null)
            return;

        new Thread(() -> {
            List<ChatMessage> messages = homeDao.getRecentMessages(currentUser.getId());

            Platform.runLater(() -> {
                
                if (messages.size() != chatContainer.getChildren().size()) {
                    chatContainer.getChildren().clear();
                    for (ChatMessage msg : messages) {
                        chatContainer.getChildren().add(createMessageBubble(msg));
                    }
                    scrollToBottom();
                }
            });
        }).start();
    }

    
    private HBox createMessageBubble(ChatMessage msg) {
        
        HBox row = new HBox(10);
        row.getStyleClass().add("msg-row"); 

        boolean isMe = msg.isMyMessage();

        if (isMe) {
            
            row.setAlignment(Pos.CENTER_RIGHT);

            
            VBox bubble = new VBox();
            
            bubble.getStyleClass().add("msg-bubble-sent");

            Label text = new Label(msg.getMessage());
            text.getStyleClass().add("msg-text-sent");
            text.setWrapText(true);

            bubble.getChildren().add(text);
            row.getChildren().add(bubble); 
        } else {
            
            row.setAlignment(Pos.CENTER_LEFT);

            
            ImageView avatar = new ImageView();
            try {
                String url = (msg.getSenderAvatar() == null || msg.getSenderAvatar().isEmpty())
                        ? "https://ui-avatars.com/api/?name=" + msg.getSenderName().replaceAll(" ", "+")
                        : msg.getSenderAvatar();
                if (url.startsWith("/userAvatar/")) {
                    url = new java.io.File("src/main/resources" + url).toURI().toString();
                }
                avatar.setImage(new Image(url, 32, 32, true, true, true)); 
            } catch (Exception e) {
            }

            Circle clip = new Circle(16, 16, 16);
            avatar.setClip(clip);

            
            VBox bubble = new VBox();
            
            bubble.getStyleClass().add("msg-bubble-received");

            Label sender = new Label(msg.getSenderName());
            sender.getStyleClass().add("msg-sender"); 
            sender.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748B; -fx-font-weight: bold;");

            Label text = new Label(msg.getMessage());
            text.getStyleClass().add("msg-text-received");
            text.setWrapText(true);

            bubble.getChildren().addAll(sender, text);
            row.getChildren().addAll(avatar, bubble);
        }
        return row;
    }

    @FXML
    void handleSendMessage(ActionEvent event) {
        String msgText = txtChatInput.getText().trim();
        if (msgText.isEmpty())
            return;

        User currentUser = UserSession.getInstance().getUser();
        
        homeDao.sendMessage(currentUser.getId(), msgText);

        txtChatInput.clear();

        
        loadChatMessages();
    }

    

    private void loadOnlineCount() {
        new Thread(() -> {
            int count = homeDao.countOnlineUsers();
            Platform.runLater(() -> {
                if (lblOnlineCount != null)
                    lblOnlineCount.setText(count + " người đang online");
            });
        }).start();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            chatScrollPane.layout(); 
            chatScrollPane.setVvalue(1.0);
        });
    }

    @FXML
    void handleViewAllNews(ActionEvent event) {
        navigateTo("/View/news.fxml");
    }

    private void openArticleDetail(Article article) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/article-detail.fxml"));
            Parent view = loader.load();

            ArticleDetailController controller = loader.getController();
            controller.setArticleData(article);

            StackPane contentArea = (StackPane) lblWelcome.getScene().lookup("#contentArea");
            if (contentArea != null) {
                ThemeManager.applyTheme(view);
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleExploreNow(ActionEvent event) {
        navigateToWithAnimation("/View/news.fxml");
    }

    @FXML
    void handlePersonalStats(ActionEvent event) {
        navigateToWithAnimation("/View/statistics.fxml");
    }

    
    private void navigateTo(String fxmlPath) {
        navigateToWithAnimation(fxmlPath);
    }

    
    private void navigateToWithAnimation(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            StackPane contentArea = (StackPane) lblWelcome.getScene().lookup("#contentArea");
            if (contentArea != null) {
                
                view.setOpacity(0);
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
                ThemeManager.applyTheme(view);

                javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(Duration.millis(300), view);
                ft.setFromValue(0.0);
                ft.setToValue(1.0);
                ft.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopPolling() {
        if (chatUpdater != null)
            chatUpdater.stop();
    }
}
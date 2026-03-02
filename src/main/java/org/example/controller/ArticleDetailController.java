package org.example.controller;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.example.model.Article;
import org.example.util.ScrollUtil;
import org.example.util.ThemeManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ArticleDetailController implements Initializable {

    @FXML private ScrollPane mainScrollPane;
    @FXML private Label lblTitle;
    @FXML private Label lblMeta;
    @FXML private ImageView imgCover;
    @FXML private Label lblContent;
    @FXML private Label lblTags;

    
    
    @FXML private HeaderController headerController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ScrollUtil.applySmoothScrolling(mainScrollPane);

        
        if (headerController != null) {
            headerController.setTitle("Chi tiết bài viết");
            headerController.showThemeButton(true); 
        }

        
        Platform.runLater(() -> {
            if (lblTitle.getScene() != null) {
                ThemeManager.applyTheme(lblTitle.getScene().getRoot());
            }
        });
    }

    public void setArticleData(Article article) {
        if (article == null) return;

        lblTitle.setText(article.getTitle());
        lblMeta.setText(article.getCreatedAt() + " • 5 phút đọc");

        if (lblTags != null) {
            lblTags.setText(article.getTags() != null ? article.getTags() : "#TechNews");
        }

        
        

        String realContent = (article.getContent() != null && !article.getContent().trim().isEmpty())
                ? article.getContent()
                : article.getDescription();

        lblContent.setText(realContent);

        try {
            if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
                imgCover.setImage(new Image(article.getImageUrl(), 850, 450, false, true));
            }
        } catch (Exception e) {
            System.err.println("Lỗi load ảnh bài báo: " + e.getMessage());
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/news.fxml"));
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
}
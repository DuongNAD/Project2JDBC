package org.example.controller;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.dao.ArticleDao;
import org.example.model.Article;
import org.example.util.ScrollUtil;
import org.example.util.ThemeManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class NewsController implements Initializable {

    @FXML private FlowPane allNewsContainer;
    @FXML private HeaderController headerController;
    @FXML private ScrollPane mainScrollPane;

    private ArticleDao articleDao = new ArticleDao();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (headerController != null) {
            headerController.setTitle("Tin tức công nghệ");
            headerController.showThemeButton(true);
        }

        ScrollUtil.applySmoothScrolling(mainScrollPane);

        
        Platform.runLater(() -> {
            if (allNewsContainer.getScene() != null) {
                ThemeManager.applyTheme(allNewsContainer.getScene().getRoot());
            }
        });

        loadNewsAsync();
    }

    private void loadNewsAsync() {
        Task<List<Article>> task = new Task<>() {
            @Override
            protected List<Article> call() {
                return articleDao.getAllArticles();
            }
        };

        task.setOnSucceeded(e -> {
            allNewsContainer.getChildren().clear();
            List<Article> articles = task.getValue();
            if (articles.isEmpty()) {
                Label emptyLbl = new Label("Chưa có tin tức nào.");
                emptyLbl.getStyleClass().add("text-description");
                allNewsContainer.getChildren().add(emptyLbl);
            } else {
                for (Article a : articles) {
                    allNewsContainer.getChildren().add(createNewsCard(a));
                }
            }
        });

        task.setOnFailed(e -> {
            System.err.println("Lỗi tải tin tức!");
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    private VBox createNewsCard(Article a) {
        VBox card = new VBox(10);
        card.setPrefWidth(300);
        card.getStyleClass().add("news-card-vertical");

        
        card.setOnMouseClicked(e -> openArticleDetail(a,e));

        ImageView img = new ImageView();
        img.setFitWidth(300);
        img.setFitHeight(160);
        img.setPreserveRatio(false);

        try {
            if (a.getImageUrl() != null && !a.getImageUrl().isEmpty()) {
                img.setImage(new Image(a.getImageUrl(), 300, 160, false, true));
            } else {
                throw new Exception("No Image");
            }
        } catch (Exception e) {
            img.setImage(new Image(getClass().getResourceAsStream("/View/avatar.jpg")));
        }

        
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(300, 160);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        img.setClip(clip);

        VBox content = new VBox(8);
        content.setStyle("-fx-padding: 0 15 15 15;"); 

        Label title = new Label(a.getTitle());
        title.getStyleClass().add("news-title");
        title.setWrapText(true);
        title.setPrefHeight(45);

        Label desc = new Label(a.getDescription());
        desc.getStyleClass().add("news-desc");
        desc.setWrapText(true);
        desc.setPrefHeight(60);
        VBox.setVgrow(desc, Priority.ALWAYS);

        Label meta = new Label((a.getTags() != null ? a.getTags() : "#News") + " • " + a.getCreatedAt());
        meta.getStyleClass().add("news-meta");

        content.getChildren().addAll(title, desc, meta);
        card.getChildren().addAll(img, content);

        return card;
    }

    private void openArticleDetail(Article article, javafx.event.Event event) {
        org.example.util.Navigation.toSync(
                event,
                org.example.util.Navigation.ARTICLE_DETAIL_VIEW,
                (org.example.controller.ArticleDetailController controller) -> {
                    controller.setArticleData(article);
                }
        );
    }

    @FXML
    void handleBack(ActionEvent event) {
        org.example.util.Navigation.to(event, org.example.util.Navigation.NEWS_VIEW);
    }
}
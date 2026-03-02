package org.example.util;

import javafx.animation.FadeTransition;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.function.Consumer;

public class Navigation {

    private static final String SHELL_FXML = "/View/hello-view.fxml"; 
    private static final String LOADING_FXML = "/View/loading.fxml";  
    public static final String LOGIN_VIEW = "/login.fxml";
    
    public static final String HOME_VIEW = "/View/home.fxml";
    public static final String MY_COURSES_VIEW = "/View/my-courses.fxml";
    public static final String COURSES_VIEW = "/View/courses.fxml";
    public static final String NEWS_VIEW = "/View/news.fxml";
    public static final String SETTINGS_VIEW = "/View/settings.fxml";
    public static final String LEARNING_VIEW = "/View/learning.fxml";
    public static final String CODING_VIEW = "/View/coding_practice.fxml";
    public static final String COURSE_DETAIL_VIEW = "/View/course-detail.fxml";
    public static final String ARTICLE_DETAIL_VIEW = "/View/article-detail.fxml";;
    public static final String STATISTICS_VIEW =  "/View/statistics.fxml";
    
    public static <T> void toSync(Event event, String fxmlPath, Consumer<T> onLoaded) {
        try {
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            Parent currentRoot = stage.getScene().getRoot();

            
            StackPane contentArea = (StackPane) currentRoot.lookup("#contentArea");

            
            if (contentArea == null) {
                FXMLLoader shellLoader = new FXMLLoader(Navigation.class.getResource(SHELL_FXML));
                Parent shellRoot = shellLoader.load();
                ThemeManager.applyTheme(shellRoot);
                stage.getScene().setRoot(shellRoot);
                contentArea = (StackPane) shellRoot.lookup("#contentArea");
            }

            if (contentArea != null) {
                
                FXMLLoader loader = new FXMLLoader(Navigation.class.getResource(fxmlPath));
                Parent targetView = loader.load();
                ThemeManager.applyTheme(targetView);

                
                targetView.setOpacity(0);
                contentArea.getChildren().clear();
                contentArea.getChildren().add(targetView);

                FadeTransition fade = new FadeTransition(Duration.millis(300), targetView);
                fade.setFromValue(0);
                fade.setToValue(1);
                fade.play();

                
                if (onLoaded != null) {
                    onLoaded.accept(loader.getController());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Lỗi Navigation Sync: " + e.getMessage());
        }
    }

    public static <T> void to(Event event, String fxmlPath, Consumer<T> onLoaded) {
        try {
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            Parent currentRoot = stage.getScene().getRoot();

            StackPane contentArea = (StackPane) currentRoot.lookup("#contentArea");

            if (contentArea == null) {
                FXMLLoader shellLoader = new FXMLLoader(Navigation.class.getResource(SHELL_FXML));
                Parent shellRoot = shellLoader.load();
                ThemeManager.applyTheme(shellRoot);
                stage.getScene().setRoot(shellRoot);
                contentArea = (StackPane) shellRoot.lookup("#contentArea");
            }

            if (contentArea != null) {
                startAsyncLoad(contentArea, fxmlPath, onLoaded);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Lỗi Navigation Async: " + e.getMessage());
        }
    }

    public static void to(Event event, String fxmlPath) {
        to(event, fxmlPath, null);
    }

    private static <T> void startAsyncLoad(StackPane area, String fxmlPath, Consumer<T> callback) {
        try {

            FXMLLoader loadingLoader = new FXMLLoader(Navigation.class.getResource(LOADING_FXML));
            Parent loadingView = loadingLoader.load();
            ThemeManager.applyTheme(loadingView);

            area.getChildren().clear();
            area.getChildren().add(loadingView);

            Task<FXMLLoader> loadTask = new Task<>() {
                @Override
                protected FXMLLoader call() throws Exception {
                    FXMLLoader loader = new FXMLLoader(Navigation.class.getResource(fxmlPath));
                    loader.load();
                    return loader;
                }
            };

            loadTask.setOnSucceeded(e -> {
                try {
                    FXMLLoader loader = loadTask.getValue();
                    Parent targetView = loader.getRoot();
                    ThemeManager.applyTheme(targetView);

                    targetView.setOpacity(0);
                    area.getChildren().clear();
                    area.getChildren().add(targetView);

                    FadeTransition fade = new FadeTransition(Duration.millis(400), targetView);
                    fade.setFromValue(0);
                    fade.setToValue(1);
                    fade.play();

                    if (callback != null) {
                        callback.accept(loader.getController());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            loadTask.setOnFailed(e -> {
                System.err.println("Lỗi tải trang ngầm: " + loadTask.getException().getMessage());
                loadTask.getException().printStackTrace();
            });

            new Thread(loadTask).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
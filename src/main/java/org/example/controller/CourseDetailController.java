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

import org.example.service.RecommendationService;
import javafx.geometry.Pos;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CourseDetailController implements Initializable {

    @FXML
    private ImageView imgThumbnail;
    @FXML
    private Label lblCategory;
    @FXML
    private Label lblTitle;
    @FXML
    private Label lblInstructor;
    @FXML
    private Label lblPrice;
    @FXML
    private Text txtDescription;
    @FXML
    private Button btnEnroll;
    @FXML
    private VBox curriculumContainer;
    @FXML
    private ScrollPane mainScrollPane;

    @FXML
    private VBox relatedSection;
    @FXML
    private HBox relatedContainer;

    @FXML
    private Label lblAverageRating;
    @FXML
    private Label lblStarsAverage;
    @FXML
    private Label lblTotalReviews;
    @FXML
    private Label lblRating;
    @FXML
    private Label lblReviewCount;
    @FXML
    private VBox ratingBarsContainer;
    @FXML
    private VBox writeReviewContainer;
    @FXML
    private HBox starSelectionBox;
    @FXML
    private Label lblRatingText;
    @FXML
    private TextArea txtReviewContent;
    @FXML
    private VBox reviewsListContainer;

    private CourseDao courseDao = new CourseDao();
    private org.example.dao.ReviewDao reviewDao = new org.example.dao.ReviewDao();
    private RecommendationService recommendationService = new RecommendationService();

    private int selectedRating = 5;

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
        if (course == null)
            return;

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

        }

        loadCurriculum(course.getCourseId());
        loadRelatedCourses(course.getCourseId());
        loadReviewsData(course.getCourseId());
    }

    private void loadReviewsData(int courseId) {
        // Load Statistics
        double avgRating = reviewDao.getAverageRating(courseId);
        int totalReviews = reviewDao.getTotalReviews(courseId);

        String formattedRating = String.format("%.1f", avgRating);

        if (lblAverageRating != null)
            lblAverageRating.setText(formattedRating);
        if (lblRating != null)
            lblRating.setText("⭐ " + formattedRating);
        if (lblTotalReviews != null)
            lblTotalReviews.setText("Từ " + totalReviews + " đánh giá");
        if (lblReviewCount != null)
            lblReviewCount.setText("(" + totalReviews + " đánh giá)");

        if (lblStarsAverage != null) {
            lblStarsAverage.setText(getStarsString((int) Math.round(avgRating)));
        }

        // Show/Hide Write Review Box
        org.example.model.User currentUser = UserSession.getInstance().getUser();
        if (currentUser != null && reviewDao.hasUserEnrolled(currentUser.getId(), courseId)) {
            if (writeReviewContainer != null) {
                writeReviewContainer.setVisible(true);
                writeReviewContainer.setManaged(true);
                setupStarSelection();
            }
        } else {
            if (writeReviewContainer != null) {
                writeReviewContainer.setVisible(false);
                writeReviewContainer.setManaged(false);
            }
        }

        // Load Reviews List
        if (reviewsListContainer != null) {
            reviewsListContainer.getChildren().clear();
            List<org.example.model.Review> reviews = reviewDao.getReviewsByCourseId(courseId);

            if (reviews.isEmpty()) {
                Label emptyLabel = new Label("Chưa có đánh giá nào cho khóa học này.");
                emptyLabel.setStyle("-fx-text-fill: #A0AEC0; -fx-font-style: italic;");
                reviewsListContainer.getChildren().add(emptyLabel);
            } else {
                for (org.example.model.Review r : reviews) {
                    reviewsListContainer.getChildren().add(createReviewCard(r));
                }
            }
        }
    }

    private void setupStarSelection() {
        if (starSelectionBox == null)
            return;
        starSelectionBox.getChildren().clear();
        selectedRating = 5;
        lblRatingText.setText("Tuyệt vời");

        for (int i = 1; i <= 5; i++) {
            final int starValue = i;
            Button starBtn = new Button("★");
            starBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #F6AD55; -fx-font-size: 24px; -fx-padding: 0; -fx-cursor: hand;");

            starBtn.setOnAction(e -> {
                selectedRating = starValue;
                updateStarSelectionUI();
                updateRatingText();
            });
            starSelectionBox.getChildren().add(starBtn);
        }
        updateStarSelectionUI();
    }

    private void updateStarSelectionUI() {
        for (int i = 0; i < starSelectionBox.getChildren().size(); i++) {
            Button btn = (Button) starSelectionBox.getChildren().get(i);
            if (i < selectedRating) {
                btn.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: #F6AD55; -fx-font-size: 24px; -fx-padding: 0; -fx-cursor: hand;");
            } else {
                btn.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: #CBD5E0; -fx-font-size: 24px; -fx-padding: 0; -fx-cursor: hand;");
            }
        }
    }

    private void updateRatingText() {
        if (lblRatingText == null)
            return;
        switch (selectedRating) {
            case 1:
                lblRatingText.setText("Rất tệ");
                break;
            case 2:
                lblRatingText.setText("Tệ");
                break;
            case 3:
                lblRatingText.setText("Bình thường");
                break;
            case 4:
                lblRatingText.setText("Tốt");
                break;
            case 5:
                lblRatingText.setText("Tuyệt vời");
                break;
        }
    }

    @FXML
    void handleSubmitReview(ActionEvent event) {
        if (currentCourse == null)
            return;

        org.example.model.User user = UserSession.getInstance().getUser();
        if (user == null)
            return;

        String comment = txtReviewContent.getText();

        boolean success = reviewDao.submitReview(user.getId(), currentCourse.getCourseId(), selectedRating, comment);

        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Cảm ơn bạn đã đánh giá khóa học!");
            alert.showAndWait();

            txtReviewContent.clear();
            loadReviewsData(currentCourse.getCourseId());
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Có lỗi xảy ra khi gửi đánh giá. Vui lòng thử lại!");
            alert.showAndWait();
        }
    }

    private String getStarsString(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < rating)
                stars.append("⭐");
            else
                stars.append("☆");
        }
        return stars.toString();
    }

    private VBox createReviewCard(org.example.model.Review review) {
        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        ImageView avatar = new ImageView();
        avatar.setFitWidth(40);
        avatar.setFitHeight(40);
        try {
            avatar.setImage(new Image(review.getUserAvatar(), 40, 40, true, true));
        } catch (Exception e) {
            avatar.setImage(new Image("https://i.pravatar.cc/150", 40, 40, true, true));
        }
        Circle clip = new Circle(20, 20, 20);
        avatar.setClip(clip);

        VBox userInfo = new VBox(2);
        Label nameLbl = new Label(review.getUserName());
        nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2D3748;");

        HBox ratingBox = new HBox(10);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        Label starsLbl = new Label(getStarsString(review.getRating()));
        starsLbl.setStyle("-fx-text-fill: #F6AD55; -fx-font-size: 12px;");

        Label dateLbl = new Label(review.getCreatedAt().toString().split(" ")[0]);
        dateLbl.setStyle("-fx-text-fill: #A0AEC0; -fx-font-size: 12px;");

        ratingBox.getChildren().addAll(starsLbl, dateLbl);
        userInfo.getChildren().addAll(nameLbl, ratingBox);

        header.getChildren().addAll(avatar, userInfo);

        Label commentLbl = new Label(review.getComment() != null ? review.getComment() : "");
        commentLbl.setWrapText(true);
        commentLbl.setStyle("-fx-text-fill: #4A5568; -fx-padding: 5 0 0 0;");

        card.getChildren().addAll(header, commentLbl);
        return card;
    }

    @FXML
    void handleEnroll(ActionEvent event) {
        try {

            if (UserSession.getInstance().getUser() == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Yêu cầu đăng nhập");
                alert.setHeaderText(null);
                alert.setContentText("Vui lòng đăng nhập để đăng ký khóa học này!");
                alert.showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/payment.fxml"));
            Parent root = loader.load();

            PaymentController controller = loader.getController();
            controller.setPaymentData(this.currentCourse);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Thanh toán - EduPath");

            ThemeManager.applyTheme(root);

            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi khi mở trang thanh toán: " + e.getMessage());
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/courses.fxml"));
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
        if (curriculumContainer == null)
            return;
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

    private void loadRelatedCourses(int courseId) {
        new Thread(() -> {
            List<Integer> relatedIds = recommendationService.getRelatedCourses(courseId, 5); // top 5

            Platform.runLater(() -> {
                if (relatedSection != null && relatedContainer != null) {
                    relatedContainer.getChildren().clear();
                    if (relatedIds != null && !relatedIds.isEmpty()) {
                        relatedSection.setVisible(true);
                        relatedSection.setManaged(true);
                        for (int id : relatedIds) {
                            Course c = courseDao.getCourseById(id);
                            if (c != null) {
                                relatedContainer.getChildren().add(createCourseCard(c));
                            }
                        }
                    } else {
                        relatedSection.setVisible(false);
                        relatedSection.setManaged(false);
                    }
                }
            });
        }).start();
    }

    private VBox createCourseCard(Course course) {
        VBox card = new VBox();
        card.setPrefWidth(260);
        card.getStyleClass().add("course-card");
        card.setCache(true);
        card.setCacheHint(javafx.scene.CacheHint.SPEED);

        StackPane imageContainer = new StackPane();
        ImageView imageView = new ImageView();
        imageView.setFitWidth(260);
        imageView.setFitHeight(150);
        imageView.getStyleClass().add("course-image");

        Rectangle clip = new Rectangle(260, 150);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imageView.setClip(clip);

        try {
            if (course.getThumbnailUrl() != null && !course.getThumbnailUrl().isEmpty()) {
                imageView.setImage(new Image(course.getThumbnailUrl(), 400, 0, true, true, true));
            } else {
                imageView.setImage(new Image(getClass().getResource("/View/avatar.jpg").toExternalForm(), 400, 0, true,
                        true, true));
            }
        } catch (Exception e) {
        }

        imageContainer.getChildren().add(imageView);

        Label badge = new Label("HOT");
        badge.getStyleClass().add("badge-best-seller");
        StackPane.setAlignment(badge, Pos.TOP_LEFT);
        StackPane.setMargin(badge, new javafx.geometry.Insets(10, 0, 0, 10));
        imageContainer.getChildren().add(badge);

        VBox content = new VBox();
        content.setSpacing(5);
        content.getStyleClass().add("card-content");

        Label categoryLabel = new Label(course.getCategoryName() != null ? course.getCategoryName() : "General");
        categoryLabel.getStyleClass().add("course-category");

        Label titleLabel = new Label(course.getTitle());
        titleLabel.getStyleClass().add("course-title");
        titleLabel.setWrapText(true);
        titleLabel.setPrefHeight(45);

        String priceText = String.format("%,.0f đ",
                course.getSalePrice() > 0 ? course.getSalePrice() : course.getPrice());
        Label priceLabel = new Label(priceText);
        priceLabel.getStyleClass().add("course-price-new");

        Button btnDetail = new Button("Xem chi tiết");
        btnDetail.setMaxWidth(Double.MAX_VALUE);
        btnDetail.getStyleClass().add("btn-view-detail");

        btnDetail.setOnAction(e -> {
            org.example.util.Navigation.to(e, org.example.util.Navigation.COURSE_DETAIL_VIEW,
                    (CourseDetailController controller) -> {
                        controller.setCourseData(course);
                    });
        });

        content.getChildren().addAll(categoryLabel, titleLabel, priceLabel, btnDetail);
        card.getChildren().addAll(imageContainer, content);

        return card;
    }
}
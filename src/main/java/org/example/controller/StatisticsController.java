package org.example.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import org.example.dao.StatisticsDao;
import org.example.util.ThemeManager;
import org.example.util.UserSession;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class StatisticsController implements Initializable {

    @FXML private HeaderController headerController;

    // Các thẻ chỉ số
    @FXML private Label lblTotalHours;
    @FXML private Label lblCompletedCourses;
    @FXML private Label lblInProgress;
    @FXML private Label lblAvgScore;

    // Các biểu đồ
    @FXML private BarChart<String, Number> barChartActivity;
    @FXML private PieChart pieChartSubjects;

    private StatisticsDao statsDao = new StatisticsDao();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (headerController != null) {
            headerController.setTitle("Thống kê học tập");
            headerController.showThemeButton(true);
        }

        loadRealData();

        if (barChartActivity.getScene() != null) {
            ThemeManager.applyTheme(barChartActivity.getScene().getRoot());
        } else {

            barChartActivity.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    Platform.runLater(() -> ThemeManager.applyTheme(newScene.getRoot()));
                }
            });
        }
    }

    private void loadRealData() {
        // Lấy ID user hiện tại
        int userId = UserSession.getInstance().getUser().getId();

        // 1. Load các thẻ số liệu (Chạy luồng riêng cho mượt)
        new Thread(() -> {
            int totalLessons = statsDao.getTotalLessonsCompleted(userId);
            int completedCourses = statsDao.getCompletedCoursesCount(userId);
            int inProgress = statsDao.getInProgressCoursesCount(userId);
            double avgScore = statsDao.getAverageScore(userId);

            Platform.runLater(() -> {
                lblTotalHours.setText(String.valueOf(totalLessons)); // Số bài học đã xong
                lblCompletedCourses.setText(String.valueOf(completedCourses));
                lblInProgress.setText(String.valueOf(inProgress));
                lblAvgScore.setText(String.valueOf(avgScore));
            });
        }).start();

        // 2. Load Biểu đồ Cột (7 ngày qua)
        loadBarChart(userId);

        // 3. Load Biểu đồ Tròn (Danh mục)
        loadPieChart(userId);
    }

    private void loadBarChart(int userId) {
        Map<String, Integer> weeklyData = statsDao.getWeeklyActivity(userId);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Bài học hoàn thành");

        if (weeklyData.isEmpty()) {
            // Nếu chưa học gì, hiển thị cột giả định bằng 0 cho đẹp
            series.getData().add(new XYChart.Data<>("Hôm nay", 0));
        } else {
            for (Map.Entry<String, Integer> entry : weeklyData.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
        }

        barChartActivity.getData().clear();
        barChartActivity.getData().add(series);
    }

    private void loadPieChart(int userId) {
        Map<String, Integer> categoryData = statsDao.getCourseCategoriesDistribution(userId);

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        if (categoryData.isEmpty()) {
            pieData.add(new PieChart.Data("Chưa có dữ liệu", 1));
        } else {
            for (Map.Entry<String, Integer> entry : categoryData.entrySet()) {
                String name = entry.getKey();
                int count = entry.getValue();
                pieData.add(new PieChart.Data(name + " (" + count + ")", count));
            }
        }

        pieChartSubjects.setData(pieData);
    }
}
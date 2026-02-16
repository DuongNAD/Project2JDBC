package org.example.controller;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.example.dao.CourseDao;
import org.example.model.CodingExercise;
import org.example.model.User;
import org.example.service.CodeCompiler;
import org.example.util.ThemeManager;
import org.example.util.UserSession;

import java.net.URL;
import java.util.ResourceBundle;

public class CodingPracticeController implements Initializable {

    @FXML private WebView webDescription;
    @FXML private WebView webCodeEditor;
    @FXML private TextArea txtOutput;

    private WebEngine editorEngine;
    private CodingExercise currentExercise;
    private CodeCompiler compiler = new CodeCompiler();

    // Gọi DAO
    private CourseDao courseDao = new CourseDao();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initEditor();
    }

    private void initEditor() {
        editorEngine = webCodeEditor.getEngine();
        URL url = getClass().getResource("/View/editor.html");
        if (url != null) {
            editorEngine.load(url.toExternalForm());
        }
    }

    public void setExerciseData(CodingExercise exercise) {
        this.currentExercise = exercise;

        // CSS cho đẹp
        String css = """
            <style>
                body { font-family: 'Segoe UI', sans-serif; padding: 20px; color: #334155; }
                h2 { color: #1E293B; border-bottom: 2px solid #E2E8F0; padding-bottom: 10px; }
                .theory-box { background-color: #EFF6FF; border-left: 4px solid #3B82F6; padding: 15px; margin-bottom: 15px; }
                .example-box { background-color: #F0FDF4; border-left: 4px solid #22C55E; padding: 15px; margin-top: 15px; }
                code { font-family: 'Consolas', monospace; background-color: #F1F5F9; padding: 2px 6px; color: #D946EF; }
                pre { background-color: #1E293B; color: #E2E8F0; padding: 15px; border-radius: 8px; overflow-x: auto; font-family: 'Consolas', monospace; }
                .highlight { color: #F59E0B; font-weight: bold; }
            </style>
        """;

        String htmlContent = "<html><head>" + css + "</head><body>"
                + "<h2>" + exercise.getTitle() + "</h2>"
                + (exercise.getDescription() != null ? exercise.getDescription() : "<p>Không có mô tả</p>")
                + "</body></html>";

        webDescription.getEngine().loadContent(htmlContent);

        editorEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED && exercise.getStarterCode() != null) {
                String safeCode = exercise.getStarterCode()
                        .replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
                editorEngine.executeScript("setCode(\"" + safeCode + "\")");
            }
        });
    }

    // --- NÚT CHẠY THỬ (Chỉ check) ---
    @FXML
    void handleRunCode(ActionEvent event) {
        runCodeAndCheck(false);
    }

    // --- NÚT NỘP BÀI (Lưu điểm + Next bài) ---
    @FXML
    void handleSubmit(ActionEvent event) {
        runCodeAndCheck(true);
    }

    private void runCodeAndCheck(boolean isSubmit) {
        if (currentExercise == null) return;

        txtOutput.setText(isSubmit ? "🚀 Đang chấm điểm..." : "⏳ Đang chạy thử...");
        txtOutput.setStyle("-fx-text-fill: #E2E8F0; -fx-control-inner-background: #1E1E1E;");

        Object codeObj = editorEngine.executeScript("getCode()");
        String userCode = codeObj.toString();

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return compiler.runCode(userCode, currentExercise.getLanguage());
            }
        };

        task.setOnSucceeded(e -> {
            String result = task.getValue();
            String expected = currentExercise.getExpectedOutput() != null ? currentExercise.getExpectedOutput().trim() : "";
            String actual = result.trim();

            if (actual.equals(expected)) {
                // ✅ ĐÚNG
                if (isSubmit) {
                    txtOutput.setText("✅ CHÍNH XÁC! \n+10 Điểm thành tích \nĐang chuyển bài...");
                    txtOutput.setStyle("-fx-text-fill: #4ADE80; -fx-control-inner-background: #1E1E1E; -fx-font-weight: bold;");

                    // 1. Lưu vào Database
                    User currentUser = UserSession.getInstance().getUser();
                    if (currentUser != null) {
                        courseDao.handleExerciseCompletion(
                                currentUser.getId(),
                                currentExercise.getId(),
                                currentExercise.getCourseId(),
                                10 // Cộng 10 điểm
                        );
                    }
                    // 2. Chuyển bài
                    showCongratulationAndNext();
                } else {
                    txtOutput.setText("✅ KẾT QUẢ ĐÚNG! \n(Bấm 'Nộp bài' để ghi nhận điểm)\n\n-------------------------\nOUTPUT:\n" + result);
                    txtOutput.setStyle("-fx-text-fill: #4ADE80; -fx-control-inner-background: #1E1E1E; -fx-font-weight: bold;");
                }
            } else {
                // ❌ SAI
                txtOutput.setText("❌ SAI KẾT QUẢ\n\n-------------------------\nTHỰC TẾ:\n" + result + "\n\nMONG ĐỢI:\n" + expected);
                txtOutput.setStyle("-fx-text-fill: #F87171; -fx-control-inner-background: #1E1E1E; -fx-font-weight: bold;");
            }
        });

        new Thread(task).start();
    }

    private void showCongratulationAndNext() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Làm tốt lắm!");
        alert.setHeaderText("🎉 BÀI LÀM CHÍNH XÁC!");
        alert.setContentText("Điểm và tiến độ đã được cập nhật.\nBấm OK để sang bài tiếp theo.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                loadNextExercise();
            }
        });
    }

    private void loadNextExercise() {
        CodingExercise nextExe = courseDao.getNextExercise(currentExercise.getCourseId(), currentExercise.getId());

        if (nextExe != null) {
            setExerciseData(nextExe); // Load bài mới
            txtOutput.clear();
            txtOutput.setStyle("-fx-control-inner-background: #1E1E1E;");
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Hoàn thành");
            alert.setHeaderText("🏆 CHÚC MỪNG!");
            alert.setContentText("Bạn đã hoàn thành tất cả bài tập trong khóa học này.");
            alert.showAndWait();
            handleBack(null);
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        try {
            org.example.model.Course course = courseDao.getCourseById(currentExercise.getCourseId());

            if (course == null) return;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/learning.fxml"));
            Parent root = loader.load();

            LearningController learningCtrl = loader.getController();
            learningCtrl.setCourseData(course);

            ThemeManager.applyTheme(root);
            Stage stage = (Stage) txtOutput.getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi khi quay về màn hình học tập: " + e.getMessage());
        }
    }
}
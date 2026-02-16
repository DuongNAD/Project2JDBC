package org.example.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.dao.UserDao;
import org.example.model.User;
import org.example.util.UserSession;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class LoginController {

    private static final String CLIENT_ID = "431759732134-18vnbj5ulag83ro6n73moka4ej7chb0s.apps.googleusercontent.com".trim();
    private static final String CLIENT_SECRET = "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".trim();

    private static final String REDIRECT_URI = "http://127.0.0.1:8888/callback";
    private static final String SCOPE = "email profile openid";
    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    @FXML
    private Button googleLoginButton;
    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Hyperlink forgotPasswordHyperlink;
    @FXML
    private Hyperlink registerLink;

    private UserDao userDao = new UserDao();

    public void onGoogleLoginClick(ActionEvent actionEvent) {
        googleLoginButton.setDisable(true);

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8888), 0);
            server.createContext("/callback", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                String code = null;
                if (query != null && query.contains("code=")) {
                    code = query.split("code=")[1].split("&")[0];
                }

                String response = "<html><body><h1 style='text-align:center;'>Dang nhap thanh cong! Ban co the tat tab nay.</h1></body></html>";
                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, response.length());

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }

                if (code != null) {
                    String finalCode = code;
                    new Thread(() -> {
                        handleGoogleOAuthCode(finalCode);
                        server.stop(0);
                    }).start();
                } else {
                    server.stop(0);
                }
            });
            server.start();

            String encodedRedirectUri = URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8);

            String encodedScope = URLEncoder.encode(SCOPE, StandardCharsets.UTF_8);

            String loginUrl = AUTH_URL + "?" +
                    "client_id=" + CLIENT_ID +
                    "&redirect_uri=" + encodedRedirectUri +
                    "&response_type=code" +
                    "&scope=" + encodedScope +
                    "&access_type=offline";

            System.out.println(">>> LINK GOOGLE DEBUG: " + loginUrl);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(loginUrl));
            }
        } catch (Exception e) {
            e.printStackTrace();
            googleLoginButton.setDisable(false);
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở trình duyệt: " + e.getMessage());
        }
    }

    private void handleGoogleOAuthCode(String code) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String params = "client_id=" + CLIENT_ID +
                    "&client_secret=" + CLIENT_SECRET +
                    "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8) +
                    "&grant_type=authorization_code" +
                    "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8);

            HttpRequest tokenRequest = HttpRequest.newBuilder()
                    .uri(URI.create(TOKEN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(params))
                    .build();

            HttpResponse<String> tokenResponse = client.send(tokenRequest, HttpResponse.BodyHandlers.ofString());
            JsonObject tokenJson = JsonParser.parseString(tokenResponse.body()).getAsJsonObject();

            if (tokenJson.has("access_token")) {
                String accessToken = tokenJson.get("access_token").getAsString();

                HttpRequest infoRequest = HttpRequest.newBuilder()
                        .uri(URI.create(USER_INFO_URL))
                        .header("Authorization", "Bearer " + accessToken)
                        .GET()
                        .build();

                HttpResponse<String> infoResponse = client.send(infoRequest, HttpResponse.BodyHandlers.ofString());
                JsonObject userInfo = JsonParser.parseString(infoResponse.body()).getAsJsonObject();

                String name = userInfo.has("name") ? userInfo.get("name").getAsString() : "Google User";
                String email = userInfo.has("email") ? userInfo.get("email").getAsString() : "";
                String pictureUrl = userInfo.has("picture") ? userInfo.get("picture").getAsString() : "";

                Platform.runLater(() -> {

                    User user = userDao.getUserByEmail(email);

                    if (user == null) {
                        User newUser = new User();
                        newUser.setUsername(email);
                        newUser.setFullname(name);
                        newUser.setEmail(email);
                        newUser.setPasswordHash("GOOGLE_LOGIN_AUTH");
                        newUser.setRole("Student");
                        newUser.setAvatarUrl(pictureUrl);

                        boolean isRegistered = userDao.register(newUser);
                        if (isRegistered) {
                            user = userDao.getUserByEmail(email);
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo tài khoản mới từ Google!");
                            return;
                        }
                    }

                    if (user != null) {
                        if (Boolean.FALSE.equals(user.getIsActive())) {
                            showAlert(Alert.AlertType.ERROR, "Lỗi", "Tài khoản này đã bị khóa!");
                            return;
                        }

                        UserSession.getInstance().setUser(user);

                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xin chào: " + user.getFullname());
                        switchToHome();
                    }
                });

            } else {
                System.out.println("Lỗi Token Response: " + tokenResponse.body());
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Lỗi Đăng Nhập", "Google từ chối xác thực.\n" + tokenJson.toString());
                    googleLoginButton.setDisable(false);
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Exception: " + e.getMessage());
                googleLoginButton.setDisable(false);
            });
        }
    }

    public void onLoginButtonClick(ActionEvent actionEvent) {
        String loginKey = usernameTextField.getText();
        String password = passwordField.getText();
        if(loginKey.trim().isEmpty() || password.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR,"Cảnh báo","Vui lòng nhập đầy đủ thông tin");
            return;
        }

        User user = userDao.login(loginKey, password);

        if(user != null) {
            UserSession.getInstance().setUser(user);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xin chào, " + user.getFullname() + "!");

            switchToHome();
        }
        else {
            showAlert(Alert.AlertType.ERROR, "Thất bại", "Sai thông tin hoặc tài khoản bị khóa!");
        }
    }

    public void onRegisterLinkClick(ActionEvent actionEvent) {
        switchScene("/register.fxml", "Đăng Ký Tài Khoản");
    }

    private void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,"Lỗi", "Không tìm thấy file giao diện: " + fxmlPath);
        }
    }

    private void switchToHome() {

        try {
            java.net.URL url = getClass().getResource("/View/hello-view.fxml");

            if (url == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Nghiêm Trọng", "Không tìm thấy file FXML!\nHãy Rebuild lại dự án.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("CườngLearn - Dashboard Học Tập");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Lỗi khi nạp giao diện: " + e.getMessage());
        }
    }

    public void onForgotPasswordClick(ActionEvent actionEvent) {
        switchScene("/forgotPassword.fxml", "Khôi Phục Mật Khẩu");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
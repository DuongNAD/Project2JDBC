package org.example.util;

import javafx.scene.Parent;
import javafx.scene.Scene;

public class ThemeManager {
    private static boolean isDarkMode = false; // Mặc định là Sáng

    public static boolean isDarkMode() {
        return isDarkMode;
    }

    public static void setDarkMode(boolean darkMode) {
        isDarkMode = darkMode;
    }

    // Hàm áp dụng theme cho bất kỳ giao diện nào (gọi khi chuyển trang)
    public static void applyTheme(Parent root) {
        if (root == null) return;

        // Xóa class cũ để tránh trùng lặp
        root.getStyleClass().remove("dark-theme");

        if (isDarkMode) {
            root.getStyleClass().add("dark-theme");
        }
    }
}
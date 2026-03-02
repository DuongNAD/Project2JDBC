package org.example.util;

import javafx.scene.Parent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    private static boolean isDarkMode = false; 
    private static final List<WeakReference<Parent>> trackedRoots = new ArrayList<>();

    public static boolean isDarkMode() {
        return isDarkMode;
    }

    public static void setDarkMode(boolean darkMode) {
        isDarkMode = darkMode;
        applyToAllTrackedRoots();
    }

    
    public static void applyTheme(Parent root) {
        if (root == null)
            return;

        
        trackRoot(root);

        
        root.getStyleClass().remove("dark-theme");

        if (isDarkMode) {
            root.getStyleClass().add("dark-theme");
        }
    }

    private static void trackRoot(Parent root) {
        
        trackedRoots.removeIf(ref -> ref.get() == null);
        for (WeakReference<Parent> ref : trackedRoots) {
            if (ref.get() == root) {
                return; 
            }
        }
        trackedRoots.add(new WeakReference<>(root));
    }

    private static void applyToAllTrackedRoots() {
        trackedRoots.removeIf(ref -> ref.get() == null);
        for (WeakReference<Parent> ref : trackedRoots) {
            Parent root = ref.get();
            if (root != null) {
                root.getStyleClass().remove("dark-theme");
                if (isDarkMode) {
                    root.getStyleClass().add("dark-theme");
                }
            }
        }
    }
}
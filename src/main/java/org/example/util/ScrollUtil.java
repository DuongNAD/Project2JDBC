package org.example.util;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;

public class ScrollUtil {
    private static final double SCROLL_SPEED = 3.5;

    public static void applySmoothScrolling(ScrollPane scrollPane) {
        if (scrollPane == null) return;

        scrollPane.setPannable(true);

        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0) {
                Node content = scrollPane.getContent();
                if (content == null) return;

                double contentHeight = content.getBoundsInLocal().getHeight();
                double viewportHeight = scrollPane.getViewportBounds().getHeight();
                double scrollableHeight = contentHeight - viewportHeight;
                if (scrollableHeight <= 0) return;
                double pixelsToScroll = event.getDeltaY() * SCROLL_SPEED;

                double percentToScroll = pixelsToScroll / scrollableHeight;
                double newVValue = scrollPane.getVvalue() - percentToScroll;
                scrollPane.setVvalue(Math.min(Math.max(newVValue, 0.0), 1.0));
                event.consume();
            }
        });
    }
}
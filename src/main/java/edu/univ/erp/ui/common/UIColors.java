package edu.univ.erp.ui.common;

import java.awt.Color;

public final class UIColors {

    public static final Color PRIMARY = new Color(0x3F51B5);
    public static final Color PRIMARY_DARK = new Color(0x303F9F);
    public static final Color PRIMARY_LIGHT = new Color(0x7986CB);
    public static final Color ACCENT = new Color(0xFF5722);

    public static final Color BACKGROUND = new Color(0xF5F5F5);
    public static final Color CARD_BACKGROUND = Color.WHITE;
    public static final Color HOVER_BACKGROUND = new Color(0xF0F0F0);

    public static final Color TEXT_PRIMARY = new Color(0x212121);
    public static final Color TEXT_SECONDARY = new Color(0x757575);
    public static final Color TEXT_HINT = new Color(0xBDBDBD);

    public static final Color SUCCESS = new Color(0x4CAF50);
    public static final Color WARNING = new Color(0xFFC107);
    public static final Color ERROR = new Color(0xF44336);
    public static final Color INFO = new Color(0x2196F3);

    public static final Color CARD_PURPLE = new Color(0x673AB7);
    public static final Color CARD_BLUE = new Color(0x2196F3);
    public static final Color CARD_GREEN = new Color(0x4CAF50);
    public static final Color CARD_ORANGE = new Color(0xFF9800);
    public static final Color CARD_TEAL = new Color(0x009688);
    public static final Color CARD_RED = new Color(0xE91E63);

    public static final Color MAINTENANCE_BG = new Color(0xFFF3E0);
    public static final Color MAINTENANCE_BORDER = new Color(0xFFB74D);
    public static final Color MAINTENANCE_TEXT = new Color(0xE65100);

    public static final Color TABLE_HEADER = new Color(0xEEEEEE);
    public static final Color TABLE_SELECTED = new Color(0xE3F2FD);
    public static final Color TABLE_ALTERNATE = new Color(0xFAFAFA);

    private UIColors() {

    }

    public static Color lighter(Color color, float factor) {
        int r = Math.min(255, (int) (color.getRed() + (255 - color.getRed()) * factor));
        int g = Math.min(255, (int) (color.getGreen() + (255 - color.getGreen()) * factor));
        int b = Math.min(255, (int) (color.getBlue() + (255 - color.getBlue()) * factor));
        return new Color(r, g, b, color.getAlpha());
    }

    public static Color darker(Color color, float factor) {
        return new Color(
            Math.max(0, (int) (color.getRed() * (1 - factor))),
            Math.max(0, (int) (color.getGreen() * (1 - factor))),
            Math.max(0, (int) (color.getBlue() * (1 - factor))),
            color.getAlpha()
        );
    }
}

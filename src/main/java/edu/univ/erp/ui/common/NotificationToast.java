package edu.univ.erp.ui.common;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

public class NotificationToast extends JDialog {

    public enum Type {
        SUCCESS, ERROR, INFO, WARNING
    }

    public NotificationToast(JFrame parent, String message, Type type) {
        super(parent, false);
        setUndecorated(true);
        setAlwaysOnTop(true);

        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(getColorForType(type), 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        panel.setBackground(getLightColorForType(type));

        JLabel iconLabel = new JLabel(getIconForType(type));
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(iconLabel, BorderLayout.WEST);

        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        messageLabel.setForeground(UIColors.TEXT_PRIMARY);
        panel.add(messageLabel, BorderLayout.CENTER);

        setContentPane(panel);
        pack();

        if (parent != null) {
            int x = parent.getX() + parent.getWidth() - getWidth() - 20;
            int y = parent.getY() + 80;
            setLocation(x, y);
        }

        Timer timer = new Timer(3000, e -> {
            setVisible(false);
            dispose();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private Color getColorForType(Type type) {
        return switch (type) {
            case SUCCESS -> UIColors.SUCCESS;
            case ERROR -> UIColors.ERROR;
            case WARNING -> UIColors.WARNING;
            case INFO -> UIColors.INFO;
        };
    }

    private Color getLightColorForType(Type type) {
        Color base = getColorForType(type);
        return UIColors.lighter(base, 0.85f);
    }

    private String getIconForType(Type type) {
        return switch (type) {
            case SUCCESS -> "✓";
            case ERROR -> "✗";
            case WARNING -> "⚠";
            case INFO -> "ℹ";
        };
    }

    public static void showSuccess(JFrame parent, String message) {
        new NotificationToast(parent, message, Type.SUCCESS).setVisible(true);
    }

    public static void showError(JFrame parent, String message) {
        new NotificationToast(parent, message, Type.ERROR).setVisible(true);
    }

    public static void showInfo(JFrame parent, String message) {
        new NotificationToast(parent, message, Type.INFO).setVisible(true);
    }

    public static void showWarning(JFrame parent, String message) {
        new NotificationToast(parent, message, Type.WARNING).setVisible(true);
    }
}

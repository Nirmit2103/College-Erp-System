package edu.univ.erp.ui.common;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

public class BannerPanel extends JPanel {

    private final JLabel messageLabel = new JLabel();

    public BannerPanel(String message) {
        setLayout(new BorderLayout());
        setBackground(UIColors.INFO);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIColors.PRIMARY_DARK),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        messageLabel.setText(message);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        add(messageLabel, BorderLayout.CENTER);
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }
}


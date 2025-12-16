package edu.univ.erp.ui.common;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

public class SecondaryButton extends JButton {

    public SecondaryButton(String text) {
        super(text);
        configure();
    }

    private void configure() {
        setFocusPainted(false);
        setBackground(Color.WHITE);
        setForeground(UIColors.PRIMARY);
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setOpaque(true);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(UIColors.PRIMARY, 2),
            javax.swing.BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        setPreferredSize(new Dimension(120, 40));
    }
}

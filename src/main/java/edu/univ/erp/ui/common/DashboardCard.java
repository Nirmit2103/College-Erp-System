package edu.univ.erp.ui.common;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DashboardCard extends JPanel {

    private final Color baseColor;
    private boolean hovered = false;
    private final JLabel iconLabel;
    private final JLabel titleLabel;
    private final JLabel subtitleLabel;

    public DashboardCard(String icon, String title, String subtitle, Color color) {
        this.baseColor = color;
        setLayout(new BorderLayout(12, 12));
        setPreferredSize(new Dimension(200, 140));
        setMinimumSize(new Dimension(180, 120));
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setForeground(Color.WHITE);
        add(iconLabel, BorderLayout.NORTH);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BorderLayout(0, 4));
        textPanel.setOpaque(false);

        titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        textPanel.add(titleLabel, BorderLayout.NORTH);

        if (subtitle != null && !subtitle.isEmpty()) {
            subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            subtitleLabel.setForeground(new Color(255, 255, 255, 200));
            textPanel.add(subtitleLabel, BorderLayout.CENTER);
        } else {
            subtitleLabel = null;
        }

        add(textPanel, BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    public DashboardCard(String icon, String title, Color color) {
        this(icon, title, null, color);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        if (hovered) {
            g2.setColor(new Color(0, 0, 0, 20));
            g2.fillRoundRect(2, 2, width - 4, height - 4, 16, 16);
        }

        Color topColor = hovered ? UIColors.lighter(baseColor, 0.1f) : baseColor;
        Color bottomColor = hovered ? baseColor : UIColors.darker(baseColor, 0.1f);

        java.awt.GradientPaint gradient = new java.awt.GradientPaint(
            0, 0, topColor,
            0, height, bottomColor
        );
        g2.setPaint(gradient);
        g2.fillRoundRect(0, 0, width, height, 12, 12);

        g2.setColor(UIColors.darker(baseColor, 0.2f));
        g2.drawRoundRect(0, 0, width - 1, height - 1, 12, 12);

        g2.dispose();
        super.paintComponent(g);
    }
}

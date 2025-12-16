package edu.univ.erp.ui;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import edu.univ.erp.AppBootstrap;
import edu.univ.erp.ui.auth.LoginController;
import edu.univ.erp.ui.auth.LoginFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.Color;
import java.awt.Font;

public final class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private Main() {
    }

    public static void main(String[] args) {
        configureLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            AppBootstrap bootstrap = new AppBootstrap();
            LoginFrame frame = new LoginFrame();
            new LoginController(bootstrap, frame);
            frame.setVisible(true);
        });
    }

    private static void configureLookAndFeel() {
        try {

            FlatIntelliJLaf.setup();

            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("ScrollBar.width", 12);
            UIManager.put("Table.rowHeight", 32);
            UIManager.put("Table.showHorizontalLines", false);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.intercellSpacing", new java.awt.Dimension(0, 1));
            UIManager.put("TabbedPane.tabHeight", 40);
            UIManager.put("TabbedPane.selectedBackground", Color.WHITE);

            Font defaultFont = new Font("Segoe UI", Font.PLAIN, 13);
            Font titleFont = new Font("Segoe UI", Font.BOLD, 14);
            UIManager.put("defaultFont", defaultFont);
            UIManager.put("Label.font", defaultFont);
            UIManager.put("Button.font", defaultFont);
            UIManager.put("Table.font", defaultFont);
            UIManager.put("TabbedPane.font", titleFont);

            FlatLaf.updateUI();
        } catch (RuntimeException ex) {
            log.warn("Failed to initialize FlatLaf IntelliJ theme, falling back to system look and feel", ex);
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                log.error("Unable to set system look and feel", e);
            }
        }
    }
}


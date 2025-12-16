package edu.univ.erp.ui.common;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

public class LoadingDialog extends JDialog {

    private final JLabel messageLabel;
    private final JProgressBar progressBar;

    public LoadingDialog(JFrame parent, String title) {
        super(parent, title, true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setUndecorated(true);

        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(UIColors.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIColors.PRIMARY, 2),
            BorderFactory.createEmptyBorder(25, 35, 25, 35)
        ));

        JLabel iconLabel = new JLabel("⏳");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(iconLabel, BorderLayout.NORTH);

        messageLabel = new JLabel(title);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        messageLabel.setForeground(UIColors.TEXT_PRIMARY);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(messageLabel, BorderLayout.CENTER);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(200, 8));
        panel.add(progressBar, BorderLayout.SOUTH);

        setContentPane(panel);
        pack();
        setLocationRelativeTo(parent);
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    public void setProgress(int value) {
        progressBar.setIndeterminate(false);
        progressBar.setValue(value);
    }
}

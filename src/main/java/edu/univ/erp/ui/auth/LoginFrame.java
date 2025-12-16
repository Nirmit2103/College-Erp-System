package edu.univ.erp.ui.auth;

import edu.univ.erp.ui.common.PrimaryButton;
import edu.univ.erp.ui.common.UIColors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JButton loginButton = new PrimaryButton("Login");
    private final JLabel attemptWarningLabel = new JLabel("");

    public LoginFrame() {
        setTitle("University ERP System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        buildUi();
    }

    private void buildUi() {

        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                java.awt.GradientPaint gradient = new java.awt.GradientPaint(
                    0, 0, UIColors.PRIMARY,
                    getWidth(), getHeight(), UIColors.PRIMARY_DARK
                );
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        mainPanel.setOpaque(true);

        JPanel centerPanel = new JPanel(null);
        centerPanel.setOpaque(false);

        JPanel loginCard = createLoginCard();
        loginCard.setBounds(300, 120, 300, 360);
        centerPanel.add(loginCard);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel createLoginCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 30), 1),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        JLabel titleLabel = new JLabel("Welcome Back");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(UIColors.TEXT_PRIMARY);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(titleLabel);

        card.add(Box.createVerticalStrut(5));

        JLabel subtitleLabel = new JLabel("Sign in to your account");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(UIColors.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(subtitleLabel);

        card.add(Box.createVerticalStrut(30));

        JPanel usernameWrapper = new JPanel();
        usernameWrapper.setLayout(new BoxLayout(usernameWrapper, BoxLayout.Y_AXIS));
        usernameWrapper.setOpaque(false);

        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userLabel.setForeground(UIColors.TEXT_SECONDARY);
        userLabel.setAlignmentX(CENTER_ALIGNMENT);
        usernameWrapper.add(userLabel);

        usernameWrapper.add(Box.createVerticalStrut(5));

        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIColors.TEXT_HINT, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        usernameWrapper.add(usernameField);

        card.add(usernameWrapper);
        card.add(Box.createVerticalStrut(15));

        JPanel passwordWrapper = new JPanel();
        passwordWrapper.setLayout(new BoxLayout(passwordWrapper, BoxLayout.Y_AXIS));
        passwordWrapper.setOpaque(false);

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        passLabel.setForeground(UIColors.TEXT_SECONDARY);
        passLabel.setAlignmentX(CENTER_ALIGNMENT);
        passwordWrapper.add(passLabel);

        passwordWrapper.add(Box.createVerticalStrut(5));

        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIColors.TEXT_HINT, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        passwordWrapper.add(passwordField);

        card.add(passwordWrapper);
        card.add(Box.createVerticalStrut(10));

        attemptWarningLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        attemptWarningLabel.setForeground(new Color(220, 53, 69));
        attemptWarningLabel.setAlignmentX(CENTER_ALIGNMENT);
        attemptWarningLabel.setVisible(false);
        card.add(attemptWarningLabel);

        card.add(Box.createVerticalStrut(15));

        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginButton.setAlignmentX(CENTER_ALIGNMENT);
        card.add(loginButton);

        card.add(Box.createVerticalStrut(15));

        return card;
    }

    public void setOnLogin(ActionListener listener) {
        loginButton.addActionListener(listener);

        getRootPane().setDefaultButton(loginButton);
    }

    public String getUsername() {
        return usernameField.getText().trim();
    }

    public JTextField getUsernameField() {
        return usernameField;
    }

    public char[] getPassword() {
        return passwordField.getPassword();
    }

    public void clearPassword() {
        passwordField.setText("");
    }

    public void showFailedAttempts(int count) {
        if (count > 0) {
            String message = count == 1 
                ? "⚠ 1 failed login attempt for this account"
                : String.format("⚠ %d failed login attempts for this account", count);
            if (count >= 3) {
                message += " - Account may be locked soon!";
            }
            attemptWarningLabel.setText(message);
            attemptWarningLabel.setVisible(true);
        } else {
            attemptWarningLabel.setVisible(false);
        }
    }

    public void hideFailedAttempts() {
        attemptWarningLabel.setVisible(false);
    }

    public void initializeWarningCheck() {

        if (!usernameField.getText().trim().isEmpty()) {

            usernameField.setText(usernameField.getText());
        }
    }
}


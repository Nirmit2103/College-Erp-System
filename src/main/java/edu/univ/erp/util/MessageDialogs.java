package edu.univ.erp.util;

import javax.swing.JOptionPane;
import java.awt.Component;

public final class MessageDialogs {

    private MessageDialogs() {
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}


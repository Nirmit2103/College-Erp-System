package edu.univ.erp.ui.common;

import edu.univ.erp.access.MaintenanceModeGuard;
import edu.univ.erp.domain.MaintenanceSetting;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class MaintenanceBanner extends JPanel implements PropertyChangeListener {

    private final MaintenanceModeGuard guard;
    private final JLabel messageLabel = new JLabel();
    private final JLabel iconLabel = new JLabel();

    public MaintenanceBanner(MaintenanceModeGuard guard) {
        this.guard = guard;
        guard.addListener(this);

        setLayout(new BorderLayout(12, 0));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, UIColors.MAINTENANCE_BORDER),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        add(iconLabel, BorderLayout.WEST);
        add(messageLabel, BorderLayout.CENTER);

        updateMessage(guard.currentSetting());
    }

    private void updateMessage(MaintenanceSetting setting) {
        boolean maintenanceOn = setting != null && setting.maintenanceOn();

        if (maintenanceOn) {
            setBackground(UIColors.MAINTENANCE_BG);
            messageLabel.setForeground(UIColors.MAINTENANCE_TEXT);
            iconLabel.setForeground(UIColors.MAINTENANCE_TEXT);
            iconLabel.setText("⚠️");
            messageLabel.setText("Maintenance Mode Active - Changes are temporarily disabled for non-admin users");
        } else {
            setBackground(UIColors.lighter(UIColors.SUCCESS, 0.8f));
            messageLabel.setForeground(UIColors.darker(UIColors.SUCCESS, 0.3f));
            iconLabel.setForeground(UIColors.darker(UIColors.SUCCESS, 0.3f));
            iconLabel.setText("✓");
            messageLabel.setText("System Operating Normally");
        }

        setVisible(true);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Object newValue = evt.getNewValue();
        if (newValue instanceof MaintenanceSetting setting) {
            updateMessage(setting);
        }
    }

    public void detach() {
        guard.removeListener(this);
    }
}

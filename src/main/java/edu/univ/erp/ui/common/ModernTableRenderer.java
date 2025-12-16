package edu.univ.erp.ui.common;

import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.SwingConstants;

public class ModernTableRenderer extends DefaultTableCellRenderer {

    private final boolean centerAlign;

    public ModernTableRenderer() {
        this(false);
    }

    public ModernTableRenderer(boolean centerAlign) {
        this.centerAlign = centerAlign;
        if (centerAlign) {
            setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (!isSelected) {
            if (row % 2 == 0) {
                c.setBackground(Color.WHITE);
            } else {
                c.setBackground(UIColors.TABLE_ALTERNATE);
            }
            c.setForeground(UIColors.TEXT_PRIMARY);
        } else {
            c.setBackground(UIColors.TABLE_SELECTED);
            c.setForeground(UIColors.PRIMARY);
        }

        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        return c;
    }
}

package edu.univ.erp.ui.dashboard;

import edu.univ.erp.domain.GradeView;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

class GradeTableModel extends AbstractTableModel {

    private final String[] columns = {
            "Course Code",
            "Title",
            "Component",
            "Score",
            "Final %",
            "Letter"
    };

    private List<GradeView> rows = new ArrayList<>();

    public void setRows(List<GradeView> rows) {
        this.rows = new ArrayList<>(rows);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        GradeView row = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> row.courseCode();
            case 1 -> row.courseTitle();
            case 2 -> row.componentName();
            case 3 -> row.score();
            case 4 -> row.finalPercentage();
            case 5 -> row.letterGrade();
            default -> "";
        };
    }
}


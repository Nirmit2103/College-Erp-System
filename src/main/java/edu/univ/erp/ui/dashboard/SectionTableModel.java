package edu.univ.erp.ui.dashboard;

import edu.univ.erp.api.types.SectionRow;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

class SectionTableModel extends AbstractTableModel {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final String[] columns = {
            "Course Code",
            "Title",
            "Credits",
            "Instructor",
            "Day",
            "Start",
            "End",
            "Room",
            "Capacity",
            "Semester",
            "Year"
    };

    private List<SectionRow> rows = new ArrayList<>();

    public void setRows(List<SectionRow> rows) {
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
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 2 -> Integer.class;
            case 8 -> Integer.class;
            case 10 -> Integer.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        SectionRow row = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> row.courseCode();
            case 1 -> row.courseTitle();
            case 2 -> row.credits();
            case 3 -> row.instructorName();
            case 4 -> row.dayOfWeek().toString();
            case 5 -> TIME_FORMAT.format(row.startTime());
            case 6 -> TIME_FORMAT.format(row.endTime());
            case 7 -> row.room();
            case 8 -> row.capacity();
            case 9 -> row.semester();
            case 10 -> row.year();
            default -> "";
        };
    }

    public SectionRow getRow(int index) {
        return rows.get(index);
    }
}


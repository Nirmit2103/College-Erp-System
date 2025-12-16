package edu.univ.erp.ui.dashboard;

import edu.univ.erp.domain.TimetableEntry;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

class TimetableTableModel extends AbstractTableModel {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final String[] columns = {
            "Day",
            "Start",
            "End",
            "Course Code",
            "Course Title",
            "Room"
    };

    private List<TimetableEntry> entries = new ArrayList<>();

    public void setEntries(List<TimetableEntry> entries) {
        this.entries = new ArrayList<>(entries);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return entries.size();
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
        TimetableEntry entry = entries.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> entry.dayOfWeek();
            case 1 -> TIME_FORMAT.format(entry.startTime());
            case 2 -> TIME_FORMAT.format(entry.endTime());
            case 3 -> entry.courseCode();
            case 4 -> entry.courseTitle();
            case 5 -> entry.room();
            default -> "";
        };
    }
}


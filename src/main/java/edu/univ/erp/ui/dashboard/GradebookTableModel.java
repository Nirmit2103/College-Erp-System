package edu.univ.erp.ui.dashboard;

import edu.univ.erp.domain.AssessmentComponent;
import edu.univ.erp.service.instructor.dto.GradebookRow;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class GradebookTableModel extends AbstractTableModel {

    private List<AssessmentComponent> components = List.of();
    private List<GradebookRow> rows = List.of();
    private ScoreUpdater scoreUpdater;

    @FunctionalInterface
    public interface ScoreUpdater {
        void update(long enrollmentId, long componentId, double score);
    }

    public void setData(List<AssessmentComponent> components, List<GradebookRow> rows) {
        this.components = Collections.unmodifiableList(new ArrayList<>(components));
        this.rows = Collections.unmodifiableList(new ArrayList<>(rows));
        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return components.size() + 3;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Roll Number";
        }
        if (column <= components.size()) {
            AssessmentComponent component = components.get(column - 1);
            return component.name() + " (" + component.weightPercentage() + "%) [/100]";
        }
        if (column == components.size() + 1) {
            return "Final %";
        }
        return "Letter";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0 || columnIndex == components.size() + 2) {
            return String.class;
        }
        return Double.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        GradebookRow row = rows.get(rowIndex);
        if (columnIndex == 0) {
            return row.rollNumber();
        }
        if (columnIndex <= components.size()) {
            AssessmentComponent component = components.get(columnIndex - 1);
            Map<Long, Double> scores = row.componentScores();
            return scores.getOrDefault(component.id(), null);
        }
        if (columnIndex == components.size() + 1) {
            return row.finalPercentage();
        }
        return row.letterGrade();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {

        return columnIndex >= 1 && columnIndex <= components.size();
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex >= 1 && columnIndex <= components.size()) {
            GradebookRow row = rows.get(rowIndex);
            AssessmentComponent component = components.get(columnIndex - 1);
            Double value = null;
            if (aValue instanceof Number) {
                value = ((Number) aValue).doubleValue();
            } else {
                try {
                    value = Double.parseDouble(String.valueOf(aValue));
                } catch (NumberFormatException ignored) {
                }
            }
            if (value != null && scoreUpdater != null) {
                scoreUpdater.update(row.enrollmentId(), component.id(), value);
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    public void setScoreUpdater(ScoreUpdater updater) {
        this.scoreUpdater = updater;
    }

    public GradebookRow getRow(int modelRow) {
        return rows.get(modelRow);
    }

    public AssessmentComponent getComponentForColumn(int columnIndex) {
        if (columnIndex >= 1 && columnIndex <= components.size()) {
            return components.get(columnIndex - 1);
        }
        return null;
    }
}


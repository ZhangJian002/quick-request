package io.github.zjay.plugin.quickrequest.complete;

import com.intellij.openapi.project.Project;
import com.intellij.util.textCompletion.TextFieldWithCompletion;
import io.github.zjay.plugin.quickrequest.config.Constant;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;

public class GeneralTextAutoCompleteEditor extends AbstractCellEditor implements TableCellEditor {

    private final TextFieldWithCompletion textField;

    public GeneralTextAutoCompleteEditor(Project project, Constant.AutoCompleteType autoCompleteType) {
        textField = new TextFieldWithCompletion(project, new GeneralTextAutoComplete(autoCompleteType), "", true, true, false);
    }


    @Override
    public Object getCellEditorValue() {
        return textField.getText();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        textField.setText(value.toString());
        return textField;
    }
    @Override
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            return ((MouseEvent)anEvent).getClickCount() >= 2;
        }
        return true;
    }
}

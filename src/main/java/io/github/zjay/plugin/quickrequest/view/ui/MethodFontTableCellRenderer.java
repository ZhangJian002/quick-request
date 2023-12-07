package io.github.zjay.plugin.quickrequest.view.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class MethodFontTableCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // 调用父类的渲染方法，以保留原有的样式
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel jLabel = (JLabel) component;
        jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
        // 根据元素值设置字体
        jLabel.setForeground(MethodFontListCellRenderer.getColor((String) value));
        return component;
    }

}

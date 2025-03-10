package io.github.zjay.plugin.quickrequest.view.ui;

import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;

public class MethodFontListCellRenderer extends DefaultListCellRenderer  {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        // 调用父类方法以获取默认的单元格组件
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        // 根据元素值设置字体
        label.setForeground(getColor((String) value));
        return label;
    }

    public static Color getColor(String type){
        if (type == null)
            return JBColor.WHITE;
        switch (type){
            case "GET":
                return Color.decode("#389fd6");
            case "POST":
                return Color.decode("#59a869");
            case "PUT":
                return Color.decode("#eda200");
            case "DELETE":
                return Color.decode("#db5860");
            case "PATCH":
                return Color.decode("#6a5acd");
            case "DUBBO":
                return Color.decode("#e23dda");
            case "GRPC":
                return Color.decode("#3BED53");
            default:
                return JBColor.WHITE;
        }
    }
}

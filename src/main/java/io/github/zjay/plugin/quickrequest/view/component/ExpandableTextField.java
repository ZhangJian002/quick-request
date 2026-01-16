package io.github.zjay.plugin.quickrequest.view.component;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class ExpandableTextField extends JBTextField {
    private JTextArea overlayTextArea;
    private JScrollPane scrollPane;
    private boolean isSyncing = false;

    public ExpandableTextField() {
        // 监听文档变化
        getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateState(); }
            public void removeUpdate(DocumentEvent e) { updateState(); }
            public void changedUpdate(DocumentEvent e) { updateState(); }
        });

        // 焦点监听
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateState();
            }
        });
        // 监听组件本身的大小、位置变化
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateOverlaySize();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                updateOverlaySize();
            }
        });

        // 关键：监听父容器层级变化（比如拖动边框导致的重布局）
        this.addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
            @Override
            public void ancestorMoved(HierarchyEvent e) {
                updateOverlaySize();
            }

            @Override
            public void ancestorResized(HierarchyEvent e) {
                updateOverlaySize();
            }
        });

        // 增加鼠标点击监听：确保点击即检查状态
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                updateState(); // 点击时强制检查一次
            }
        });

    }

    private void updateState() {
        if (isSyncing) return;

        FontMetrics fm = getFontMetrics(getFont());
        int textWidth = fm.stringWidth(getText() == null ? "" : getText());

        // 只有超长且没显示悬浮层时才开启
        if (textWidth > getWidth() - 20) {
//            showOverlay();
            if (overlayTextArea == null) {
                showOverlay();
            } else {
                updateOverlaySize(); // 如果已经存在，强制刷新位置和高度
            }
        }
    }

    private void showOverlay() {
        if (overlayTextArea != null) return;

        JRootPane rootPane = SwingUtilities.getRootPane(this);
        if (rootPane == null) return;
        JLayeredPane layeredPane = rootPane.getLayeredPane();

        overlayTextArea = new JBTextArea(getText());
        overlayTextArea.setLineWrap(true);
        overlayTextArea.setWrapStyleWord(false);
        overlayTextArea.setFont(getFont());

        // 同步逻辑
        overlayTextArea.getDocument().addDocumentListener(new DocumentListener() {
            private void sync() {
                if (isSyncing) return;
                isSyncing = true;
                try {
                    ExpandableTextField.super.setText(overlayTextArea.getText());
                    updateOverlaySize();
                } finally {
                    isSyncing = false;
                }
            }
            public void insertUpdate(DocumentEvent e) { sync(); }
            public void removeUpdate(DocumentEvent e) { sync(); }
            public void changedUpdate(DocumentEvent e) { sync(); }
        });

        // 3. 失去焦点立即销毁（解决点击两次的问题）
        overlayTextArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                removeOverlay();
            }
            @Override
            public void focusGained(FocusEvent e) {
                if (scrollPane != null) scrollPane.repaint(); // 变蓝色
            }
        });

        scrollPane = new JBScrollPane(overlayTextArea);
        scrollPane.setBorder(new RoundBorder(10));

        // 先计算一次位置，再添加
        updateOverlaySize();

        layeredPane.add(scrollPane, JLayeredPane.POPUP_LAYER);

        // 重点：使用 invokeLater 确保焦点和显示同步
        SwingUtilities.invokeLater(() -> {
            if (overlayTextArea != null) {
                overlayTextArea.requestFocusInWindow();
                overlayTextArea.setCaretPosition(overlayTextArea.getText().length());
                updateOverlaySize(); // 二次确认位置，防止添加瞬间的位移
            }
        });

        // 强制重绘
        layeredPane.validate();
        layeredPane.repaint();
    }

    private void updateOverlaySize() {
        // 关键：去掉 !scrollPane.isShowing()，因为刚添加时它还没显示
        if (scrollPane == null) return;

        JRootPane rootPane = SwingUtilities.getRootPane(this);
        if (rootPane == null) return;

        // 重新计算相对于 LayeredPane 的实时坐标
        Point location = SwingUtilities.convertPoint(this, 0, 0, rootPane.getLayeredPane());

        FontMetrics fm = getFontMetrics(getFont());
        String text = getText() == null ? "" : getText();
        int textWidth = fm.stringWidth(text);
        int currentWidth = getWidth();
        int availableWidth = Math.max(1, currentWidth - 20);

        // 重新计算行数（支持多行高度自动扩展）
        int lines = (textWidth / availableWidth) + 1;
        // 确保高度至少能覆盖原 TextField
        int targetHeight = Math.min(Math.max(getHeight(), (lines * fm.getHeight()) + 15), 300);

        // 立即应用位置，不等待下一次重绘
        scrollPane.setBounds(location.x, location.y, currentWidth, targetHeight);

        // 强制通知 UI 布局更新
        scrollPane.revalidate();
        scrollPane.repaint();
    }


    private void removeOverlay() {
        if (scrollPane != null && scrollPane.getParent() != null) {
            Container parent = scrollPane.getParent();
            parent.remove(scrollPane);
            parent.validate();
            parent.repaint();
        }
        overlayTextArea = null;
        scrollPane = null;
    }

    @Override
    public void setText(String t) {
        super.setText(t);
        if (overlayTextArea != null && !isSyncing) {
            overlayTextArea.setText(t);
        }
    }

    static class RoundBorder extends AbstractBorder {
        private final int radius;

        public RoundBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // --- 核心修改：判断焦点状态 ---
            // 检查当前组件（通常是 JScrollPane）或者它的子组件（TextArea）是否拥有焦点
            boolean isFocused = UIUtil.isFocusAncestor(c);

            if (isFocused) {
                // 绘制 IDEA 原生的蓝色聚焦边框
                g2d.setColor(JBUI.CurrentTheme.Focus.focusColor());
                // 稍微加粗一点点，增强视觉感
                g2d.setStroke(new BasicStroke(1.5f));
            } else {
                // 默认的灰色边框
                g2d.setColor(JBColor.border());
                g2d.setStroke(new BasicStroke(1.0f));
            }

            // 绘制圆角矩形
            // 注意：如果 Stroke 加粗了，矩形的范围需要微调（-1变为-1.5等），保证边框不被剪裁
            g2d.draw(new RoundRectangle2D.Double(x + 0.5, y + 0.5, width - 1.5, height - 1.5, radius, radius));
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            // 设置内容向内缩进，防止文字贴到圆角边框上
            return JBUI.insets(5, 8);
        }
    }
}
package io.github.zjay.plugin.quickrequest.view.component;

import javax.swing.*;
import java.awt.*;

/**
 * @author zjay
 * @create 2024-07-11 下午1:38
 */
public class MaskPanel extends JPanel {
    int width;
    int height;

    public MaskPanel(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 在这里绘制遮罩层的内容
        g.setColor(new Color(0, 0, 0, 128)); // 半透明的黑色
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}

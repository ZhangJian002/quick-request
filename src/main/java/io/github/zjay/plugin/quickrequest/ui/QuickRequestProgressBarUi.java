//package io.github.zjay.plugin.fastrequest.ui;
//
//import com.intellij.openapi.ui.GraphicsConfig;
//import com.intellij.ui.Gray;
//import com.intellij.ui.JBColor;
//import com.intellij.ui.scale.JBUIScale;
//import com.intellij.util.ui.GraphicsUtil;
//import com.intellij.util.ui.JBUI;
//import com.intellij.util.ui.UIUtil;
//import quickRequest.icons.PluginIcons;
//import sun.swing.SwingUtilities2;
//
//import javax.swing.*;
//import javax.swing.plaf.ComponentUI;
//import javax.swing.plaf.basic.BasicProgressBarUI;
//import java.awt.*;
//import java.awt.event.ComponentAdapter;
//import java.awt.event.ComponentEvent;
//import java.awt.geom.*;
//
//public class QuickRequestProgressBarUi extends BasicProgressBarUI {
//    private static final float[] FRACTIONS = {0.5f, 1f};
//    private static final Color GOPHER_COLOR_START = new JBColor(new Color(1, 173, 216), new Color(1, 173, 216));
//    private static final Color GOPHER_COLOR_END = new JBColor(new Color(0, 162, 156), new Color(0, 162, 156));
//    private static final Color[] COLORS = {GOPHER_COLOR_START, GOPHER_COLOR_END};
//    private static final JBColor SHADES_OF_GREY = new JBColor(Gray._240.withAlpha(50), Gray._128.withAlpha(50));
//    private static final JBColor SHADES_OF_GREY2 = new JBColor(Gray._165.withAlpha(50), Gray._88.withAlpha(50));
//
//    @SuppressWarnings({"MethodOverridesStaticMethodOfSuperclass", "UnusedDeclaration"})
//    public static ComponentUI createUI(JComponent c) {
//        c.setBorder(JBUI.Borders.empty().asUIResource());
//        return new QuickRequestProgressBarUi();
//    }
//
//    @Override
//    public Dimension getPreferredSize(JComponent c) {
//        return new Dimension(super.getPreferredSize(c).width, JBUI.scale(20));
//    }
//
//    @Override
//    protected void installListeners() {
//        super.installListeners();
//        progressBar.addComponentListener(new ComponentAdapter() {
//            @Override
//            public void componentShown(ComponentEvent e) {
//                super.componentShown(e);
//            }
//
//            @Override
//            public void componentHidden(ComponentEvent e) {
//                super.componentHidden(e);
//            }
//        });
//    }
//
//    private volatile int offset = 0;
//    private volatile int offset2 = 0;
//    private volatile int velocity = 1;
//
//    @Override
//    protected void paintIndeterminate(Graphics g, JComponent c) {
//        if (!(g instanceof Graphics2D)) {
//            return;
//        }
//        Graphics2D g2d = (Graphics2D) g;
//
//        Insets b = progressBar.getInsets(); // area for border
//        int barRectWidth = progressBar.getWidth() - (b.right + b.left);
//        int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);
//
//        if (barRectWidth <= 0 || barRectHeight <= 0) {
//            return;
//        }
//
//        g2d.setColor(SHADES_OF_GREY);
//        int w = c.getWidth();
//        int h = c.getPreferredSize().height;
//        if (isOdd(c.getHeight() - h)) h++;
//
//        LinearGradientPaint baseRainbowPaint = new LinearGradientPaint(0, JBUI.scale(2), 0, h - JBUI.scale(2), FRACTIONS, COLORS);
//
//        g2d.setPaint(baseRainbowPaint);
//
//        if (c.isOpaque()) {
//            g2d.fillRect(0, (c.getHeight() - h) / 2, w, h);
//        }
//        g2d.setColor(SHADES_OF_GREY2);
//        final GraphicsConfig config = GraphicsUtil.setupAAPainting(g2d);
//        g2d.translate(0, (c.getHeight() - h) / 2);
//
//        Paint old = g2d.getPaint();
//        g2d.setPaint(baseRainbowPaint);
//
//        final Area containingRoundRect = new Area(new RoundRectangle2D.Float(1f, 1f, w - 2f, h - 2f, JBUIScale.scale(8f), JBUIScale.scale(8f)));
//        g2d.fill(containingRoundRect);
//        g2d.setPaint(old);
//        offset = (offset + 1) % JBUI.scale(16);
//        offset2 += velocity;
//        if (offset2 <= 2) {
//            offset2 = 2;
//            velocity = 1;
//        } else if (offset2 >= w - JBUI.scale(15)) {
//            offset2 = w - JBUI.scale(15);
//            velocity = -1;
//        }
//
//        Area area = new Area(new Rectangle2D.Float(0, 0, w, h));
//        area.subtract(new Area(new RoundRectangle2D.Float(1f, 1f, w - 2f, h - 2f, JBUIScale.scale(8f), JBUIScale.scale(8f))));
//        g2d.setPaint(Gray._128);
//
//        if (c.isOpaque()) {
//            g2d.fill(area);
//        }
//
//        area.subtract(new Area(new RoundRectangle2D.Float(0, 0, w, h, JBUIScale.scale(9f), JBUIScale.scale(9f))));
//
//        Container parent = c.getParent();
//        Color background = parent != null ? parent.getBackground() : UIUtil.getPanelBackground();
//        g2d.setPaint(background);
//
//        if (c.isOpaque()) {
//            g2d.fill(area);
//        }
//
//        g2d.draw(new RoundRectangle2D.Float(1f, 1f, w - 3f, h - 3f, JBUIScale.scale(8f), JBUIScale.scale(8f)));
//        PluginIcons.fastRequest_editor.paintIcon(progressBar, g2d, offset2 - JBUI.scale(10), -JBUI.scale(2));
//
//        // Deal with possible text painting
//        if (progressBar.isStringPainted()) {
//            if (progressBar.getOrientation() == SwingConstants.HORIZONTAL) {
//                paintString(g2d, b.left, b.top, barRectWidth, barRectHeight, boxRect.x, boxRect.width);
//            } else {
//                paintString(g2d, b.left, b.top, barRectWidth, barRectHeight, boxRect.y, boxRect.height);
//            }
//        }
//        config.restore();
//    }
//
//    @Override
//    protected void paintDeterminate(Graphics g, JComponent c) {
//        if (!(g instanceof Graphics2D)) {
//            return;
//        }
//
//        if (progressBar.getOrientation() != SwingConstants.HORIZONTAL || !c.getComponentOrientation().isLeftToRight()) {
//            super.paintDeterminate(g, c);
//            return;
//        }
//        final GraphicsConfig config = GraphicsUtil.setupAAPainting(g);
//        Insets b = progressBar.getInsets(); // area for border
//        int w = progressBar.getWidth();
//        int h = progressBar.getPreferredSize().height;
//        if (isOdd(c.getHeight() - h)) h++;
//
//        int barRectWidth = w - (b.right + b.left);
//        int barRectHeight = h - (b.top + b.bottom);
//
//        if (barRectWidth <= 0 || barRectHeight <= 0) {
//            return;
//        }
//
//        int amountFull = getAmountFull(b, barRectWidth, barRectHeight);
//
//        Container parent = c.getParent();
//        Color background = parent != null ? parent.getBackground() : UIUtil.getPanelBackground();
//        g.setColor(background);
//
//        Graphics2D g2d = (Graphics2D) g;
//        if (c.isOpaque()) {
//            g.fillRect(0, 0, w, h);
//        }
//
//        g2d.translate(0, (c.getHeight() - h) / 2);
//        g2d.setColor(progressBar.getForeground());
//        g2d.fill(new RoundRectangle2D.Float(0, 0, w - JBUIScale.scale(1f), h - JBUIScale.scale(1f), JBUIScale.scale(9f), JBUIScale.scale(9f)));
//        g2d.setColor(background);
//        g2d.fill(new RoundRectangle2D.Float(JBUIScale.scale(1f), JBUIScale.scale(1f), w - 3f, h - 3f, JBUIScale.scale(8f), JBUIScale.scale(8f)));
//
//        g2d.setPaint(new LinearGradientPaint(0, JBUI.scale(2), 0, h - JBUI.scale(2), FRACTIONS, COLORS));
//        g2d.fill(new RoundRectangle2D.Float(2f, 2f, amountFull - JBUIScale.scale(5f), h - JBUIScale.scale(5f), JBUIScale.scale(7f), JBUIScale.scale(7f)));
//
//        PluginIcons.fastRequest_editor.paintIcon(progressBar, g2d, amountFull - JBUI.scale(10), -JBUI.scale(2));
//
//        // Deal with possible text painting
//        if (progressBar.isStringPainted()) {
//            paintString(g, b.left, b.top, barRectWidth, barRectHeight, amountFull, b);
//        }
//        config.restore();
//    }
//
//    private void paintString(Graphics g, int x, int y, int w, int h, int fillStart, int amountFull) {
//        if (!(g instanceof Graphics2D)) {
//            return;
//        }
//
//        Graphics2D g2 = (Graphics2D) g;
//        String progressString = progressBar.getString();
//        g2.setFont(progressBar.getFont());
//        Point renderLocation = getStringPlacement(g2, progressString, x, y, w, h);
//        Rectangle oldClip = g2.getClipBounds();
//
//        if (progressBar.getOrientation() == SwingConstants.HORIZONTAL) {
//            g2.setColor(getSelectionBackground());
//            SwingUtilities2.drawString(progressBar, g2, progressString, renderLocation.x, renderLocation.y);
//            g2.setColor(getSelectionForeground());
//            g2.clipRect(fillStart, y, amountFull, h);
//            SwingUtilities2.drawString(progressBar, g2, progressString, renderLocation.x, renderLocation.y);
//        } else { // VERTICAL
//            g2.setColor(getSelectionBackground());
//            AffineTransform rotate = AffineTransform.getRotateInstance(Math.PI / 2);
//            g2.setFont(progressBar.getFont().deriveFont(rotate));
//            renderLocation = getStringPlacement(g2, progressString, x, y, w, h);
//            SwingUtilities2.drawString(progressBar, g2, progressString, renderLocation.x, renderLocation.y);
//            g2.setColor(getSelectionForeground());
//            g2.clipRect(x, fillStart, w, amountFull);
//            SwingUtilities2.drawString(progressBar, g2, progressString, renderLocation.x, renderLocation.y);
//        }
//        g2.setClip(oldClip);
//    }
//
//    @Override
//    protected int getBoxLength(int availableLength, int otherDimension) {
//        return availableLength;
//    }
//
//    private static boolean isOdd(int value) {
//        return value % 2 != 0;
//    }
//}

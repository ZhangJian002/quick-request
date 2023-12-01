//package io.github.zjay.plugin.fastrequest.ui;
//
//import com.intellij.ide.ui.LafManager;
//import com.intellij.ide.ui.LafManagerListener;
//import org.jetbrains.annotations.NotNull;
//
//import javax.swing.*;
//
//public class MyProgressBarUiComponent implements LafManagerListener {
//    public void lookAndFeelChanged(@NotNull LafManager source) {
//        updateProgressBarUi();
//    }
//
//    private void updateProgressBarUi() {
//        UIManager.put("ProgressBarUI", QuickRequestProgressBarUi.class.getName());
//        UIManager.getDefaults().put(QuickRequestProgressBarUi.class.getName(), QuickRequestProgressBarUi.class);
//    }
//}

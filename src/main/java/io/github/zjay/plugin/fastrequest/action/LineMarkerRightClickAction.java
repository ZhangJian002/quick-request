package io.github.zjay.plugin.fastrequest.action;

import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.AboutDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiElement;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.impl.breakpoints.ui.BreakpointsDialogFactory;
import com.intellij.xdebugger.impl.ui.BreakpointEditor;
import com.intellij.xdebugger.impl.ui.DebuggerUIUtil;
import free.icons.PluginIcons;
import io.github.zjay.plugin.fastrequest.configurable.MyLineMarkerInfo;
import io.github.zjay.plugin.fastrequest.service.GeneratorUrlService;
import io.github.zjay.plugin.fastrequest.util.ToolWindowUtil;
import io.github.zjay.plugin.fastrequest.view.FastRequestToolWindow;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LineMarkerRightClickAction extends AnAction implements DumbAware {

    private final GutterIconRenderer myRenderer;
    private final MyLineMarkerInfo myPoint;

    private final static JBPopupMenu clickIconPopupMenu  = new JBPopupMenu();

    private static final JBMenuItem clickAndSendItem = new JBMenuItem(" Generate And Send ");
    private static final JBMenuItem clickAndConfigItem = new JBMenuItem(" Configuration Management ");

    private static PsiElement psiElement;

    private static Project myProject;

    static {
        clickAndSendItem.setIcon(PluginIcons.ICON_SEND);
        clickAndSendItem.addActionListener(evt -> {
            GeneratorUrlService generatorUrlService = ApplicationManager.getApplication().getService(GeneratorUrlService.class);
            ToolWindowUtil.generatorUrlAndSend(myProject, generatorUrlService, psiElement.getParent(), true);
        });
        clickAndConfigItem.setIcon(AllIcons.General.Settings);
        clickAndConfigItem.addActionListener(evt -> {
            ShowSettingsUtil.getInstance().showSettingsDialog(myProject, "Quick Request");
        });
        clickIconPopupMenu.add(clickAndSendItem);
        clickIconPopupMenu.addSeparator();
        clickIconPopupMenu.add(clickAndConfigItem);
    }


    public LineMarkerRightClickAction(MyLineMarkerInfo point, GutterIconRenderer renderer){
        myRenderer = renderer;
        myPoint = point;
        psiElement = point.getElement();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        Project project = getEventProject(e);
        if (editor == null || project == null) return;
        myProject = project;
        EditorGutterComponentEx gutterComponent = ((EditorEx)editor).getGutterComponentEx();
        Point point = gutterComponent.getCenterPoint(myRenderer);
        if (point == null) { // disabled gutter icons for example
            point = new Point(gutterComponent.getWidth(),
                    editor.visualPositionToXY(editor.getCaretModel().getVisualPosition()).y + editor.getLineHeight() / 2);
        }
        clickIconPopupMenu.show(gutterComponent, point.x, point.y);

    }
}

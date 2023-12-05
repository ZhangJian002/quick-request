package io.github.zjay.plugin.fastrequest.action;

import com.intellij.icons.AllIcons;
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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import io.github.zjay.plugin.fastrequest.generator.impl.GoMethodGenerator;
import io.github.zjay.plugin.fastrequest.util.LanguageEnum;
import io.github.zjay.plugin.fastrequest.util.ToolUtils;
import io.github.zjay.plugin.fastrequest.util.go.GoMethod;
import io.github.zjay.plugin.fastrequest.view.linemarker.tooltip.BaseFunctionTooltip;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import quickRequest.icons.PluginIcons;
import io.github.zjay.plugin.fastrequest.configurable.MyLineMarkerInfo;
import io.github.zjay.plugin.fastrequest.service.GeneratorUrlService;
import io.github.zjay.plugin.fastrequest.util.ToolWindowUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class LineMarkerRightClickAction extends AnAction implements DumbAware {

    private final GutterIconRenderer myRenderer;
    private final MyLineMarkerInfo myPoint;

    private JBPopupMenu clickIconPopupMenu;

    private Project myProject;

    void createPopMenu() {
        clickIconPopupMenu  = new JBPopupMenu();
        JBMenuItem clickAndSendItem = new JBMenuItem(" Generate And Send ");
        clickAndSendItem.setIcon(PluginIcons.ICON_SEND);
        clickAndSendItem.addActionListener(evt -> {
            PsiElement psiElement = myPoint.getElement();
            BaseFunctionTooltip functionTooltip = myPoint.getFunctionTooltip();
            LanguageEnum language = functionTooltip.getLanguage();
            switch (language){
                case java:
                case Kotlin:
                    //java、Kotlin
                    GeneratorUrlService generatorUrlService = ApplicationManager.getApplication().getService(GeneratorUrlService.class);
                    ToolWindowUtil.generatorUrlAndSend(myProject, generatorUrlService, functionTooltip.getElement(), true);
                    break;
                case go:
                    //Go
                    String method = GoMethod.getMethodType(psiElement.getText());
                    ApplicationManager.getApplication().getService(GoMethodGenerator.class).generate(functionTooltip.getElement(), method, null);
                    ToolWindowUtil.openToolWindow(myProject);
                    ToolWindowUtil.sendRequest(myProject, true);
                    break;
            }
        });
        clickIconPopupMenu.add(clickAndSendItem);
        clickIconPopupMenu.addSeparator();
        JBMenuItem clickAndConfigItem = new JBMenuItem(" Configuration Management ");
        clickAndConfigItem.setIcon(AllIcons.General.Settings);
        clickAndConfigItem.addActionListener(evt -> {
            ShowSettingsUtil.getInstance().showSettingsDialog(myProject, "Quick Request");
        });
        clickIconPopupMenu.add(clickAndConfigItem);
    }


    public LineMarkerRightClickAction(MyLineMarkerInfo point, GutterIconRenderer renderer){
        myRenderer = renderer;
        myPoint = point;
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
        createPopMenu();
        clickIconPopupMenu.show(gutterComponent, point.x, point.y);

    }
}

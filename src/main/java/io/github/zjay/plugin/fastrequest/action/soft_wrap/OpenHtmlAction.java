package io.github.zjay.plugin.fastrequest.action.soft_wrap;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class OpenHtmlAction extends AnAction {

    public OpenHtmlAction(){
        super("Open as Html", "Open as html", AllIcons.Xml.Browsers.Chrome);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent paramAnActionEvent) {
        if (paramAnActionEvent == null) return;
        Editor editor = (Editor)paramAnActionEvent.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        if(!editor.isViewer()){
            return;
        }
        Project project = paramAnActionEvent.getProject();
        if (project == null) {
            return;
        }
        String str = editor.getDocument().getText();
        HTMLEditorProvider.openEditor(project, "Html Preview", str);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = (Editor)e.getData(CommonDataKeys.EDITOR);
        if(editor.isViewer()){
            e.getPresentation().setEnabledAndVisible(true);
        }else {
            e.getPresentation().setEnabledAndVisible(false);
        }
    }
}

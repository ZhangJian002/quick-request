package io.github.zjay.plugin.quickrequest.config.action.soft_wrap;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class OpenHtmlAction extends AnAction {

    public OpenHtmlAction(){
        super("Open as Html", "Open as html", AllIcons.Xml.Browsers.Chrome);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent paramAnActionEvent) {
        if (paramAnActionEvent == null) return;
        Editor editor = (Editor)paramAnActionEvent.getData(CommonDataKeys.EDITOR);
        if (editor == null || !editor.isViewer()) {
            return;
        }
        Project project = paramAnActionEvent.getProject();
        if (project == null) {
            return;
        }
        String str = editor.getDocument().getText();
        if(StringUtils.isNotBlank(str)){
            HTMLEditorProvider.openEditor(project, "Html Preview", str);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
//        super.update(e);
        Presentation presentation = e.getPresentation();
        if (presentation.isEnabled()) {
            Editor editor = e.getData(CommonDataKeys.EDITOR);
            if (editor != null && e.getProject() != null) {
                PsiFile file = PsiDocumentManager.getInstance(e.getProject()).getPsiFile(editor.getDocument());
                if (file != null && file.getVirtualFile() != null) {
                    e.getPresentation().setEnabledAndVisible(file.getName().startsWith("ZJay."));
                }
            } else {
                e.getPresentation().setEnabledAndVisible(false);
            }
        }
    }


}

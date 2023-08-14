package io.github.zjay.plugin.fastrequest.action.soft_wrap;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import io.github.zjay.plugin.fastrequest.config.Constant;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class FormatAction extends AnAction {

    public FormatAction(){
        super("Format", "Format", AllIcons.Diff.MagicResolve);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent paramAnActionEvent) {
        Project project = paramAnActionEvent.getProject();
        if (project == null) {
            return;
        }
        Editor editor = paramAnActionEvent.getData(CommonDataKeys.EDITOR);
        if (editor == null || !editor.getDocument().isWritable()) {
            return;
        }
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (psiFile != null && StringUtils.isNotBlank(editor.getDocument().getText())) {
            WriteCommandAction.runWriteCommandAction(project, (Computable<PsiElement>) () -> CodeStyleManager.getInstance(project).reformat(psiFile));
        }
    }

    @Override
    public void update(@NotNull AnActionEvent paramAnActionEvent) {
        Project project = paramAnActionEvent.getProject();
        if (project == null) {
            paramAnActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }
        Editor editor = paramAnActionEvent.getData(CommonDataKeys.EDITOR);
        if (editor == null || !editor.getDocument().isWritable() || editor.isViewer()) {
            paramAnActionEvent.getPresentation().setEnabledAndVisible(false);
            return;
        }
        Presentation presentation = paramAnActionEvent.getPresentation();
        if (presentation.isEnabled()) {
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
            if (virtualFile != null) {
                if(virtualFile.isWritable()){
                    Integer integer = editor.getUserData(Constant.KEY_QUICKREQUEST);
                    paramAnActionEvent.getPresentation().setEnabledAndVisible(integer != null);
                }else {
                    paramAnActionEvent.getPresentation().setEnabledAndVisible(false);
                }
            }else {
                paramAnActionEvent.getPresentation().setEnabledAndVisible(false);
            }
        }else {
            paramAnActionEvent.getPresentation().setEnabledAndVisible(false);
        }
    }

}

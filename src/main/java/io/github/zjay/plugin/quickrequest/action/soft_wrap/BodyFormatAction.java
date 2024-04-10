package io.github.zjay.plugin.quickrequest.action.soft_wrap;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import io.github.zjay.plugin.quickrequest.base.ParentAction;
import io.github.zjay.plugin.quickrequest.config.Constant;
import io.github.zjay.plugin.quickrequest.util.ToolWindowUtil;
import io.github.zjay.plugin.quickrequest.util.http.BodyContentType;
import io.github.zjay.plugin.quickrequest.view.FastRequestCollectionToolWindow;
import io.github.zjay.plugin.quickrequest.view.FastRequestToolWindow;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import quickRequest.icons.PluginIcons;

import javax.swing.*;
import java.awt.*;

public class BodyFormatAction extends ParentAction {

    public static volatile String chooseBodyType = "JSON";

    private static final FastRequestCollectionToolWindow.MyActionGroup myActionGroup = new FastRequestCollectionToolWindow.MyActionGroup();

    public BodyFormatAction(){
        super(PluginIcons.ICON_JSON);
    }

    public BodyFormatAction(Icon icon){
        super(icon);
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
            initPopMenus(project);
            // 显示弹出窗口
            Dimension dimension = editor.getComponent().getSize();
            Point point = editor.getComponent().getLocationOnScreen();

            Point point1 = new Point();
            point1.setLocation((int) (point.getX() + dimension.getWidth() / 2 + 30), (int) (point.getY() + 30));

            JBPopupFactory.getInstance().createActionGroupPopup(null, myActionGroup, paramAnActionEvent.getDataContext(), true, null, 10)
                    .showInScreenCoordinates(editor.getComponent(), point1);
        }
    }

    public void initPopMenus(Project project){
        if(myActionGroup.isNotEmpty()){
            return;
        }
        for (BodyContentType bodyContentType : BodyContentType.values()) {
            myActionGroup.add(new ParentAction(bodyContentType.getName()) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    DefaultActionGroup actionGroup = (DefaultActionGroup)ActionManager.getInstance().getAction("quickRequest.editor.floatGroup");
                    AnAction[] children = actionGroup.getChildren(e);
                    for (AnAction child : children) {
                        if (child instanceof BodyFormatAction) {
                            actionGroup.replaceAction(child, new BodyFormatAction(bodyContentType.getIcon()));
                            break;
                        }
                    }
                    chooseBodyType = bodyContentType.getName();
                    FastRequestToolWindow fastRequestToolWindow = ToolWindowUtil.getFastRequestToolWindow(project);
                    if (fastRequestToolWindow != null) {
                        fastRequestToolWindow.setBodyFormat(bodyContentType);
                    }
                }
            });
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
                    paramAnActionEvent.getPresentation().setEnabledAndVisible(integer != null && integer == 1);
                }else {
                    paramAnActionEvent.getPresentation().setEnabledAndVisible(false);
                }
            }else {
                paramAnActionEvent.getPresentation().setEnabledAndVisible(false);
            }
        }else {
            paramAnActionEvent.getPresentation().setEnabledAndVisible(false);
        }
        if(editor.getDocument().getTextLength() == 0){
            paramAnActionEvent.getPresentation().setEnabled(false);
        }
    }

}

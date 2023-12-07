package io.github.zjay.plugin.quickrequest.action.soft_wrap;

import com.intellij.icons.AllIcons;
import com.intellij.idea.ActionsBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actions.AbstractToggleUseSoftWrapsAction;
import com.intellij.openapi.editor.impl.softwrap.SoftWrapAppliancePlaces;
import io.github.zjay.plugin.quickrequest.config.Constant;
import org.jetbrains.annotations.NotNull;

public class SoftWrapAction extends AbstractToggleUseSoftWrapsAction {
    /**
     * Creates new {@code AbstractToggleUseSoftWrapsAction} object.
     *
     * @param appliancePlace defines type of the place where soft wraps are applied
     * @param global         indicates if soft wraps should be changed for the current editor only or for the all editors
     *                       used at the target appliance place
     */
    public SoftWrapAction(@NotNull SoftWrapAppliancePlaces appliancePlace, boolean global) {
        super(appliancePlace, global);
    }

    public SoftWrapAction() {
        super(SoftWrapAppliancePlaces.MAIN_EDITOR, false);
    }

    @Override
    public void update(@NotNull AnActionEvent paramAnActionEvent) {
        Editor editor = (Editor) paramAnActionEvent.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        Integer integer = (Integer)editor.getUserData(Constant.KEY_QUICKREQUEST);
        paramAnActionEvent.getPresentation().setIcon(AllIcons.Actions.ToggleSoftWrap);
        if (integer != null) {
            paramAnActionEvent.getPresentation().setText(ActionsBundle.messagePointer("action.EditorGutterToggleLocalSoftWraps.gutterText", new Object[0]));
            paramAnActionEvent.getPresentation().setEnabledAndVisible(true);
        } else {
            paramAnActionEvent.getPresentation().setEnabledAndVisible(false);
        }

    }
}

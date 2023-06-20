package io.github.zjay.plugin.fastrequest.action.soft_wrap;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.InspectionWidgetActionProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuickRequestToggleAction implements InspectionWidgetActionProvider {

    @Nullable
    @Override
    public AnAction createAction(@NotNull Editor paramEditor) {
        return ActionManager.getInstance().getAction("quickRequest.editor.floatGroup");
    }
}

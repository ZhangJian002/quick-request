/*
 * Copyright 2021 zjay(darzjay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.zjay.plugin.quickrequest.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import io.github.zjay.plugin.quickrequest.base.ParentDumbAction;
import io.github.zjay.plugin.quickrequest.util.ToolWindowUtil;
import io.github.zjay.plugin.quickrequest.view.FastRequestToolWindow;
import org.jetbrains.annotations.NotNull;
import quickRequest.icons.PluginIcons;

public class ToolbarSendRequestAction extends ParentDumbAction {

    public ToolbarSendRequestAction() {
        super(() -> "Send", PluginIcons.ICON_SEND);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project myProject = e.getData(LangDataKeys.PROJECT);
        if (myProject == null) {
            return;
        }
        FastRequestToolWindow fastRequestToolWindow = ToolWindowUtil.getFastRequestToolWindow(myProject);
        if (fastRequestToolWindow != null) {
            fastRequestToolWindow.sendRequestEvent(false);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        ToolbarSendAndDownloadRequestAction.updateVisit(e);
    }
}
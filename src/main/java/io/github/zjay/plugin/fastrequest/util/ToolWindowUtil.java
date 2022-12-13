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

package io.github.zjay.plugin.fastrequest.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.content.Content;
import com.intellij.util.messages.MessageBus;
import io.github.zjay.plugin.fastrequest.config.FastRequestComponent;
import io.github.zjay.plugin.fastrequest.configurable.ConfigChangeNotifier;
import io.github.zjay.plugin.fastrequest.model.FastRequestConfiguration;
import io.github.zjay.plugin.fastrequest.service.GeneratorUrlService;
import io.github.zjay.plugin.fastrequest.view.FastRequestToolWindow;

public class ToolWindowUtil {
    public static FastRequestToolWindow getFastRequestToolWindow(Project myProject) {
        final ToolWindow toolWindow = ToolWindowManager.getInstance(myProject).getToolWindow("Fast Request Free");
        if (toolWindow != null) {
            final Content content = toolWindow.getContentManager().getContent(0);
            if (content != null) {
                return (FastRequestToolWindow) content.getComponent();
            }
        }
        return null;
    }

    public static void generatorUrl(Project project, GeneratorUrlService generatorUrlService, PsiElement methodElement) {
        generatorUrlService.generate(methodElement);
        //打开工具窗口
        ToolWindow fastRequestToolWindow = ToolWindowManager.getInstance(project).getToolWindow("Fast Request Free");
        if (fastRequestToolWindow != null && !fastRequestToolWindow.isActive()) {
            fastRequestToolWindow.activate(null);
            Content content = fastRequestToolWindow.getContentManager().getContent(0);
            assert content != null;
            fastRequestToolWindow.getContentManager().setSelectedContent(content);
        }
        //send message to change param
        MessageBus messageBus = project.getMessageBus();
        messageBus.connect();
        ConfigChangeNotifier configChangeNotifier = messageBus.syncPublisher(ConfigChangeNotifier.PARAM_CHANGE_TOPIC);
        configChangeNotifier.configChanged(true, project.getName());
    }

    public static void generatorUrlAndSend(Project project, GeneratorUrlService generatorUrlService, PsiElement methodElement, boolean sendFlag){
        generatorUrl(project, generatorUrlService, methodElement);
        FastRequestConfiguration state = FastRequestComponent.getInstance().getState();
        FastRequestToolWindow fastRequestToolWindow = getFastRequestToolWindow(project);
        if (fastRequestToolWindow != null && state != null) {
            if (sendFlag || (state.getClickAndSend() != null && state.getClickAndSend())) {
                fastRequestToolWindow.sendRequestEvent(false);
            }
        }
    }
}

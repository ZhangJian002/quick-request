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

package io.github.zjay.plugin.quickrequest.configurable;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import io.github.zjay.plugin.quickrequest.model.CollectionConfiguration;
import io.github.zjay.plugin.quickrequest.model.HistoryTableData;
import io.github.zjay.plugin.quickrequest.view.AllApisNavToolWindow;
import io.github.zjay.plugin.quickrequest.view.FastRequestCollectionToolWindow;
import io.github.zjay.plugin.quickrequest.view.FastRequestToolWindow;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FastRequestToolWindowFactory implements ToolWindowFactory, DumbAware {

    private final Map<String, FastRequestToolWindow> windowMap = new ConcurrentHashMap<>();
    private final Map<String, FastRequestCollectionToolWindow> apiWindowMap = new ConcurrentHashMap<>();
    private final Map<String, AllApisNavToolWindow> allApisNavToolWindowMap = new ConcurrentHashMap<>();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        FastRequestToolWindow window = new FastRequestToolWindow(toolWindow, project);
        FastRequestCollectionToolWindow collectionToolWindow = new FastRequestCollectionToolWindow(project, toolWindow);
        windowMap.put(project.getName(), window);
        apiWindowMap.put(project.getName(), collectionToolWindow);

        ContentFactory contentFactory = ApplicationManager.getApplication().getService(ContentFactory.class);
        window.getComponent().add(window.getContent());
        Content content = contentFactory.createContent(window, "Request", true);
        content.setIcon(AllIcons.General.RunWithCoverage);
        content.putUserData(ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
        toolWindow.getContentManager().addContent(content);

        Content contentCollection = contentFactory.createContent(collectionToolWindow.getContent(), "APIs", true);
        contentCollection.setIcon(AllIcons.Nodes.PpLib);
        contentCollection.putUserData(ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
        toolWindow.getContentManager().addContent(contentCollection);

        AllApisNavToolWindow allApisNavToolWindow = new AllApisNavToolWindow(project, toolWindow);
        allApisNavToolWindowMap.put(project.getName(), allApisNavToolWindow);
        allApisNavToolWindow.getComponent().add(allApisNavToolWindow.getContent());
        Content allApis = contentFactory.createContent(allApisNavToolWindow, "Navigate", true);
        allApis.setIcon(AllIcons.Ide.LocalScopeAction);
        allApis.putUserData(ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
        toolWindow.getContentManager().addContent(allApis);
//        if(ToolUtils.isSupportAction()){
//
//        }
        //隐藏标题
        toolWindow.getComponent().putClientProperty("HideIdLabel", "true");


        //change data
        MessageBus messageBus = project.getMessageBus();
        MessageBusConnection connect = messageBus.connect();

        connect.subscribe(ConfigChangeNotifier.PARAM_CHANGE_TOPIC, new ConfigChangeNotifier() {
            @Override
            public void configChanged(boolean active, String projectName) {
                windowMap.get(projectName).refresh(false);
            }
        });

        connect.subscribe(ConfigChangeNotifier.ENV_PROJECT_CHANGE_TOPIC,
                new ConfigChangeNotifier() {
                    @Override
                    public void configChanged(boolean active, String projectName) {
                        //有可能在全局配置下点击,
                        FastRequestToolWindow window = windowMap.get(projectName);
                        if (window != null) {
                            window.changeEnvAndProject();
                        }
                    }
                });

        connect.subscribe(ConfigChangeNotifier.ADD_REQUEST_TOPIC,
                new ConfigChangeNotifier() {
                    @Override
                    public void configChanged(boolean active, String projectName) {
                        apiWindowMap.get(projectName).refresh();
//                        collectionToolWindow.refresh();
                    }
                });

        connect.subscribe(ConfigChangeNotifier.LOAD_REQUEST,
                new ConfigChangeNotifier() {
                    @Override
                    public void loadRequest(CollectionConfiguration.CollectionDetail detail, String projectName, boolean sendFlag, boolean flag) {
                        windowMap.get(projectName).refreshByCollection(detail, flag);
                        if(sendFlag){
                            windowMap.get(projectName).sendRequestEvent(false);
                        }
//                    window.refreshByCollection(detail);
                    }
                });

        connect.subscribe(ConfigChangeNotifier.LOAD_REQUEST_HISTORY,
                new ConfigChangeNotifier() {
                    @Override
                    public void loadRequestHistory(HistoryTableData data, String projectName, boolean sendFlag, boolean flag) {
                        windowMap.get(projectName).refreshByHisCollection(data, flag);
                        if(sendFlag){
                            windowMap.get(projectName).sendRequestEvent(false);
                        }
//                    window.refreshByCollection(detail);
                    }
                });

    }
}

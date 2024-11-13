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

package io.github.zjay.plugin.quickrequest.update;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.ide.util.RunOnceUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.ui.MessageType;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import io.github.zjay.plugin.quickrequest.config.FastRequestComponent;
import io.github.zjay.plugin.quickrequest.listener.SpringBootListener;
import io.github.zjay.plugin.quickrequest.model.FastRequestConfiguration;
import io.github.zjay.plugin.quickrequest.util.MyResourceBundleUtil;
import org.jetbrains.annotations.NotNull;

public class WhatsNewActivity implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {

        MessageBus messageBus = project.getMessageBus();
        MessageBusConnection connect = messageBus.connect();
        connect.subscribe(ExecutionManager.EXECUTION_TOPIC, new ExecutionListener(){
            @Override
            public void processStarted(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler) {
                RunProfile runProfile = env.getRunProfile();
                if (runProfile instanceof ModuleBasedConfiguration){
                    handler.addProcessListener(new SpringBootListener(project, (ModuleBasedConfiguration<?,?>)runProfile));
                }
            }

            @Override
            public void processStarting(@NotNull String executorId, @NotNull ExecutionEnvironment env) {
            }
        });

        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        if(config != null && config.getParamGroup() != null){
            config.getParamGroup().clear();
        }
//        IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.getId("QuickRequest"));
//        if (plugin != null) {
//            System.out.println("当前版本：" +plugin.getVersion());
//            PluginDownloader downloader = PluginDownloader.getInstance();
//            String latestVersion = downloader.get(id);
//            Runtime.Version.parse(plugin.getVersion());
        RunOnceUtil.runOnceForApp("Quick Request", () -> {
            Notification quickRequestWindowNotificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("quickRequestWindowNotificationGroup").createNotification(MyResourceBundleUtil.getKey("Welcome"), MessageType.INFO);
            quickRequestWindowNotificationGroup.addAction(new NotificationAction(MyResourceBundleUtil.getKey("HowToUse")) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent anActionEvent, @NotNull Notification notification) {
                    BrowserUtil.browse("https://blog.csdn.net/qq_41013833/article/details/131328100");
                }
            });
            quickRequestWindowNotificationGroup.notify(project);
        });
//        }


    }
}

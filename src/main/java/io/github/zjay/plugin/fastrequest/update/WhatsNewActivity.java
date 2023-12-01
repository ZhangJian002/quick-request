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

package io.github.zjay.plugin.fastrequest.update;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.ide.util.RunOnceUtil;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.updateSettings.impl.PluginDownloader;
import io.github.zjay.plugin.fastrequest.config.Constant;
import io.github.zjay.plugin.fastrequest.util.MyResourceBundleUtil;
import io.github.zjay.plugin.fastrequest.util.ToolUtils;
import org.jetbrains.annotations.NotNull;

public class WhatsNewActivity implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.getId("QuickRequest"));
//        if (plugin != null) {
//            System.out.println("当前版本：" +plugin.getVersion());
//            PluginDownloader downloader = PluginDownloader.getInstance();
//            String latestVersion = downloader.get(id);
//            Runtime.Version.parse(plugin.getVersion());
            RunOnceUtil.runOnceForApp("Quick Request Tip", () -> {
                ApplicationManager.getApplication().invokeLater(() -> {
                    String msg = "Welcome to Quick Request. If you have any questions, please report to me https://github.com/zhangjianay/quick-request/issues.";
                    NotificationGroupManager.getInstance().getNotificationGroup("quickRequestWindowNotificationGroup").createNotification(msg, MessageType.INFO)
                            .notify(project);
                });
            });
//        }


    }
}

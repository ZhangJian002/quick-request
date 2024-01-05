package io.github.zjay.plugin.quickrequest.util;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import io.github.zjay.plugin.quickrequest.config.Constant;

public class ToolUtils {

    public static boolean isSupport(){
        ApplicationInfo applicationInfo = ApplicationManager.getApplication().getService(ApplicationInfo.class);
        return Constant.JetBrainsProductName.isExist(applicationInfo.getFullApplicationName());
    }

    public static boolean isSupportAction(){
        ApplicationInfo applicationInfo = ApplicationManager.getApplication().getService(ApplicationInfo.class);
        return Constant.JetBrainsProductName.isButtonSupport(applicationInfo.getFullApplicationName());
    }

    public static boolean isSupportKotlin(){
        IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.getId("org.jetbrains.kotlin"));
        if(plugin == null || !plugin.isEnabled()){
            return false;
        }
        return true;
    }
}

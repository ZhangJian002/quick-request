package io.github.zjay.plugin.fastrequest.util;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import io.github.zjay.plugin.fastrequest.config.Constant;

public class ToolUtils {

    public static boolean isSupport(){
        ApplicationInfo applicationInfo = ApplicationManager.getApplication().getService(ApplicationInfo.class);
        return Constant.JetBrainsProductName.isExist(applicationInfo.getFullApplicationName());
    }

    public static boolean isSupportAction(){
        ApplicationInfo applicationInfo = ApplicationManager.getApplication().getService(ApplicationInfo.class);
        return Constant.JetBrainsProductName.isButtonSupport(applicationInfo.getFullApplicationName());
    }
}

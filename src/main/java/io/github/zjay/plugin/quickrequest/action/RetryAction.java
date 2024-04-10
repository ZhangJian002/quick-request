package io.github.zjay.plugin.quickrequest.action;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.PsiNavigateUtil;
import io.github.zjay.plugin.quickrequest.base.ParentAction;
import io.github.zjay.plugin.quickrequest.config.FastRequestComponent;
import io.github.zjay.plugin.quickrequest.model.FastRequestConfiguration;
import io.github.zjay.plugin.quickrequest.model.ParamGroup;
import io.github.zjay.plugin.quickrequest.service.GeneratorUrlService;
import io.github.zjay.plugin.quickrequest.util.MyResourceBundleUtil;
import io.github.zjay.plugin.quickrequest.util.ToolWindowUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import quickRequest.icons.PluginIcons;

public class RetryAction extends ParentAction {

    private Project myProject;
    public RetryAction() {
        super(MyResourceBundleUtil.getKey("regenerate"), MyResourceBundleUtil.getKey("regenerate"), PluginIcons.ICON_REDO);
        this.myProject = myProject;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;
        ParamGroup paramGroup = config.getParamGroup();
        String className = paramGroup.getClassName();
        String methodName = paramGroup.getMethod();
        if (StringUtils.isBlank(className)) {
            NotificationGroupManager.getInstance().getNotificationGroup("quickRequestWindowNotificationGroup").createNotification("You should generate first", MessageType.ERROR)
//                        .addAction(new NotificationAction("Document") {
//                            @Override
//                            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
//                                Desktop dp = Desktop.getDesktop();
//                                if (dp.isSupported(Desktop.Action.BROWSE)) {
//                                    try {
//                                        if ("zh".equals(MyResourceBundleUtil.getKey("language"))) {
//                                            dp.browse(URI.create(Constant.CN_DOC_DOMAIN + "/guide/feature/#%E9%87%8D%E6%96%B0%E7%94%9F%E5%AD%98%E8%AF%B7%E6%B1%82"));
//                                        } else {
//                                            dp.browse(URI.create(String.format("%s/guide/getstarted/#regenetate", Constant.EN_DOC_DOMAIN)));
//                                        }
//                                    } catch (IOException ex) {
//                                        LOGGER.error("open url fail:%s/guide/getstarted/#regenetate", ex, Constant.EN_DOC_DOMAIN);
//                                    }
//                                }
//                            }
//                        })
                    .notify(myProject);
            return;
        }
        PsiClass psiClass = JavaPsiFacade.getInstance(myProject).findClass(className, GlobalSearchScope.projectScope(myProject));
        if (psiClass != null) {
            PsiElement[] elementArray = psiClass.findMethodsByName(methodName, true);
            if (elementArray.length > 0) {
                PsiMethod psiMethod = (PsiMethod) elementArray[0];
                PsiNavigateUtil.navigate(psiMethod);
                GeneratorUrlService generatorUrlService = ApplicationManager.getApplication().getService(GeneratorUrlService.class);
                generatorUrlService.generate(psiMethod);
                ToolWindowUtil.getFastRequestToolWindow(myProject).refresh(true);
            }
        }
    }
}

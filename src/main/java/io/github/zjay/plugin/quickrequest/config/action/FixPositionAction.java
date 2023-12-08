package io.github.zjay.plugin.quickrequest.config.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.PsiNavigateUtil;
import io.github.zjay.plugin.quickrequest.config.FastRequestComponent;
import io.github.zjay.plugin.quickrequest.model.FastRequestConfiguration;
import io.github.zjay.plugin.quickrequest.model.ParamGroup;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.stubindex.KotlinFullClassNameIndex;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtClassOrObject;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import quickRequest.icons.PluginIcons;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class FixPositionAction extends AnAction {

    private Project myProject;

    public FixPositionAction(Project myProject) {
        super("Focus", "Focus", PluginIcons.ICON_LOCAL_SCOPE_LARGE);
        this.myProject = myProject;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;
        ParamGroup paramGroup = config.getParamGroup();
        String className = paramGroup.getClassName();
        String methodName = paramGroup.getMethod();
        Integer type = paramGroup.getType();
        if(type == null || type == 0){
            PsiClass psiClass = JavaPsiFacade.getInstance(myProject).findClass(className, GlobalSearchScope.projectScope(myProject));
            if (psiClass != null) {
                PsiElement[] elementArray = psiClass.findMethodsByName(methodName, true);
                if (elementArray.length > 0) {
                    PsiMethod psiMethod = (PsiMethod) elementArray[0];
                    PsiNavigateUtil.navigate(psiMethod);
                }
            }
        }else {
            Collection<KtClassOrObject> ktClassOrObjects = StubIndex.getElements(KotlinFullClassNameIndex.getInstance().getKey(), className, myProject, GlobalSearchScope.projectScope(myProject), KtClassOrObject.class);
            if(CollectionUtils.isNotEmpty(ktClassOrObjects)){
                for (KtClassOrObject ktClassOrObject : ktClassOrObjects) {
                    if(ktClassOrObject instanceof KtClass){
                        KtClass ktClass = (KtClass) ktClassOrObject;
                        List<KtDeclaration> declarations = ktClass.getDeclarations();
                        for (KtDeclaration declaration : declarations) {
                            if(declaration instanceof KtNamedFunction && Objects.equals(methodName, declaration.getName())){
                                PsiNavigateUtil.navigate(declaration);
                                return;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }
}

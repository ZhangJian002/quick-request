package io.github.zjay.plugin.quickrequest.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.FileIndex;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.PsiNavigateUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.stubs.indexes.PhpClassIndex;
import io.github.zjay.plugin.quickrequest.config.FastRequestComponent;
import io.github.zjay.plugin.quickrequest.contributor.PhpRequestMappingContributor;
import io.github.zjay.plugin.quickrequest.generator.linemarker.PhpLineMarkerProvider;
import io.github.zjay.plugin.quickrequest.model.FastRequestConfiguration;
import io.github.zjay.plugin.quickrequest.model.OtherRequestEntity;
import io.github.zjay.plugin.quickrequest.model.ParamGroup;
import io.github.zjay.plugin.quickrequest.util.PhpTwoJinZhi;
import io.github.zjay.plugin.quickrequest.util.TwoJinZhiGet;
import io.github.zjay.plugin.quickrequest.util.php.LaravelMethods;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.stubindex.KotlinAnnotationsIndex;
import org.jetbrains.kotlin.idea.stubindex.KotlinFullClassNameIndex;
import org.jetbrains.kotlin.psi.*;
import quickRequest.icons.PluginIcons;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class FixPositionAction extends AnAction {

    private static KotlinFullClassNameIndex kotlinFullClassNameIndex;

    static {
        try {
            Constructor<KotlinFullClassNameIndex> declaredConstructor = KotlinFullClassNameIndex.class.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            kotlinFullClassNameIndex = declaredConstructor.newInstance();
        } catch (Exception e) {

        }
    }

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
        }else if(type == 2){
            try {
                Class.forName("org.jetbrains.kotlin.idea.stubindex.KotlinFullClassNameIndex");
                if(kotlinFullClassNameIndex == null){
                    return;
                }
                Collection<KtClassOrObject> ktClassOrObjects = StubIndex.getElements(kotlinFullClassNameIndex.getKey(), className, myProject, GlobalSearchScope.projectScope(myProject), KtClassOrObject.class);
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
            }catch (Exception e0){

            }
        }else if(type == 3){
            PsiFile[] filesByName = FilenameIndex.getFilesByName(myProject, className, GlobalSearchScope.everythingScope(myProject));
            for (PsiFile psiFile : filesByName) {
                PsiElement[] psiElements = PsiTreeUtil.collectElements(psiFile, dd -> true);
                for (PsiElement psiElement : psiElements) {
                    if(PhpRequestMappingContributor.judge(psiElement)){
                        String[] urlAndMethodName = PhpRequestMappingContributor.getUrlAndMethodName(psiElement);
                        if(urlAndMethodName != null){
                            if(Objects.equals(urlAndMethodName[1], methodName) && Objects.equals(urlAndMethodName[0], paramGroup.getOriginUrl())){
                                //找到了
                                PsiNavigateUtil.navigate(psiElement.getFirstChild().getNextSibling().getNextSibling());
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}

package io.github.zjay.plugin.quickrequest.action;

import com.goide.GoLibrariesUtil;
import com.goide.index.GoFileParentsIndex;
import com.goide.index.GoImportIndex;
import com.goide.psi.GoFieldName;
import com.goide.psi.GoKey;
import com.goide.stubs.index.GoMethodIndex;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.FileIndex;
import com.intellij.openapi.roots.FileIndexUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.include.FileIncludeIndex;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.Processors;
import com.intellij.util.PsiNavigateUtil;
import com.intellij.util.SmartList;
import com.intellij.util.indexing.FileBasedIndex;
import io.github.zjay.plugin.quickrequest.config.Constant;
import io.github.zjay.plugin.quickrequest.config.FastRequestComponent;
import io.github.zjay.plugin.quickrequest.contributor.GoRequestMappingContributor;
import io.github.zjay.plugin.quickrequest.contributor.PhpRequestMappingContributor;
import io.github.zjay.plugin.quickrequest.model.FastRequestConfiguration;
import io.github.zjay.plugin.quickrequest.model.OtherRequestEntity;
import io.github.zjay.plugin.quickrequest.model.ParamGroup;
import io.github.zjay.plugin.quickrequest.util.GoTwoJinZhi;
import io.github.zjay.plugin.quickrequest.util.TwoJinZhiGet;
import io.github.zjay.plugin.quickrequest.util.go.GoMethod;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.stubindex.KotlinFullClassNameIndex;
import org.jetbrains.kotlin.psi.*;
import quickRequest.icons.PluginIcons;

import java.lang.reflect.Constructor;
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
        this.searchAndFocus();
    }

    private void searchAndFocus() {
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
        } else if (type == 1) {
            PsiManager psiManager = PsiManager.getInstance(myProject);
            Collection<VirtualFile> virtualFiles = FilenameIndex.getAllFilesByExt(myProject, "go");
            for (VirtualFile virtualFile : virtualFiles) {
                if(Objects.equals(virtualFile.getName(), className + ".go")){
                    PsiElement[] psiElements = PsiTreeUtil.collectElements(psiManager.findFile(virtualFile), dd -> true);
                    for (PsiElement psiElement : psiElements) {
                        if (TwoJinZhiGet.getRealStr(GoTwoJinZhi.CALL_EXPR).equals((psiElement.getNode().getElementType().toString()))) {
                            if (GoMethod.isExist((psiElement.getFirstChild().getLastChild()).getText()) && Objects.equals(GoRequestMappingContributor.getUrl(psiElement), paramGroup.getOriginUrl())) {
                                //找到了
                                PsiNavigateUtil.navigate(psiElement.getFirstChild().getLastChild());
                                return;
                            }
                        }
                    }
                }
            }
        } else if(type == 2){
            try {
                Class.forName("org.jetbrains.kotlin.idea.stubindex.KotlinFullClassNameIndex");
                Constructor<KotlinFullClassNameIndex> declaredConstructor = KotlinFullClassNameIndex.class.getDeclaredConstructor();
                declaredConstructor.setAccessible(true);
                KotlinFullClassNameIndex kotlinFullClassNameIndex = declaredConstructor.newInstance();
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
            PsiManager psiManager = PsiManager.getInstance(myProject);
            Collection<VirtualFile> virtualFiles = FilenameIndex.getAllFilesByExt(myProject, "php");
            for (VirtualFile virtualFile : virtualFiles) {
                if(Objects.equals(virtualFile.getName(), className)){
                    PsiElement[] psiElements = PsiTreeUtil.collectElements(psiManager.findFile(virtualFile), dd -> true);
                    for (PsiElement psiElement : psiElements) {
                        if (PhpRequestMappingContributor.judge(psiElement)) {
                            String[] urlAndMethodName = PhpRequestMappingContributor.getUrlAndMethodName(psiElement);
                            if (urlAndMethodName != null) {
                                if (Objects.equals(urlAndMethodName[1], methodName) && Objects.equals(urlAndMethodName[0], paramGroup.getOriginUrl())) {
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
}

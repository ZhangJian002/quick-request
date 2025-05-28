package io.github.zjay.plugin.quickrequest.view.component.tree.allApis;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import io.github.zjay.plugin.quickrequest.contributor.GoRequestMappingContributor;
import io.github.zjay.plugin.quickrequest.model.ApiService;
import io.github.zjay.plugin.quickrequest.model.OtherRequestEntity;
import io.github.zjay.plugin.quickrequest.util.LanguageEnum;
import io.github.zjay.plugin.quickrequest.util.ReflectUtils;

import java.util.List;

public class GoApis extends BaseApis{
    @Override
    public List<OtherRequestEntity> getResultList(Project myProject) {
        return GoRequestMappingContributor.getResultList(myProject);
    }

    @Override
    public void setBaseName(ApiService apiService, PsiFile psiFile) {
        String name = (String)ReflectUtils.invokeMethod(psiFile, "getName");
        String packageName = (String)ReflectUtils.invokeMethod(psiFile, "getPackageName");
        apiService.setClassName(name);
        apiService.setPackageName(packageName);
        apiService.setLanguage(LanguageEnum.go);
    }
}

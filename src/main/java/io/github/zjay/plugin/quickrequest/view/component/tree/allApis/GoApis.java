package io.github.zjay.plugin.quickrequest.view.component.tree.allApis;

import com.goide.psi.GoFile;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import io.github.zjay.plugin.quickrequest.contributor.GoRequestMappingContributor;
import io.github.zjay.plugin.quickrequest.model.ApiService;
import io.github.zjay.plugin.quickrequest.model.OtherRequestEntity;
import io.github.zjay.plugin.quickrequest.util.LanguageEnum;

import java.util.List;

public class GoApis extends BaseApis{
    @Override
    public List<OtherRequestEntity> getResultList(Project myProject) {
        return GoRequestMappingContributor.getResultList(myProject);
    }

    @Override
    public void setBaseName(ApiService apiService, PsiFile psiFile) {
        GoFile containingFile = (GoFile)psiFile;
        apiService.setClassName(containingFile.getName());
        apiService.setPackageName(containingFile.getPackageName());
        apiService.setLanguage(LanguageEnum.go);
    }
}

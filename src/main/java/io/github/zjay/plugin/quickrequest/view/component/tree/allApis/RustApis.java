package io.github.zjay.plugin.quickrequest.view.component.tree.allApis;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import io.github.zjay.plugin.quickrequest.contributor.RustRequestMappingContributor;
import io.github.zjay.plugin.quickrequest.model.ApiService;
import io.github.zjay.plugin.quickrequest.model.OtherRequestEntity;
import io.github.zjay.plugin.quickrequest.util.LanguageEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class RustApis extends BaseApis{
    @Override
    public List<OtherRequestEntity> getResultList(Project myProject) {
        return RustRequestMappingContributor.getResultList(myProject);
    }

    @Override
    public void setBaseName(ApiService apiService, PsiFile psiFile) {
        apiService.setLanguage(LanguageEnum.Rust);
        apiService.setClassName(psiFile.getName().replaceFirst(".rs", ""));
        String basePath = psiFile.getProject().getBasePath();
        if(StringUtils.isNotBlank(basePath)){
            String locationString = psiFile.getPresentation().getLocationString();
            locationString = locationString.replaceAll("\\\\", "/");
            apiService.setPackageName(locationString.replaceFirst(basePath, "").replaceAll("/", ""));
        }
    }
}

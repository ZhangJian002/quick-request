package io.github.zjay.plugin.quickrequest.view.component.tree.allApis;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import io.github.zjay.plugin.quickrequest.contributor.RubyRequestMappingContributor;
import io.github.zjay.plugin.quickrequest.model.ApiService;
import io.github.zjay.plugin.quickrequest.model.OtherRequestEntity;
import io.github.zjay.plugin.quickrequest.util.LanguageEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class RubyApis extends BaseApis{
    @Override
    public List<OtherRequestEntity> getResultList(Project myProject) {
        return RubyRequestMappingContributor.getResultList(myProject);
    }

    @Override
    public void setBaseName(ApiService apiService, PsiFile psiFile) {
        apiService.setLanguage(LanguageEnum.Ruby);
        apiService.setClassName(psiFile.getName());
        String basePath = psiFile.getProject().getBasePath();
        if(StringUtils.isNotBlank(basePath)){
            String locationString = psiFile.getPresentation().getLocationString();
            locationString = locationString.replaceAll("\\\\", "/");
            apiService.setPackageName(locationString.replaceFirst(basePath, "").replaceAll("/", ""));
        }
    }
}

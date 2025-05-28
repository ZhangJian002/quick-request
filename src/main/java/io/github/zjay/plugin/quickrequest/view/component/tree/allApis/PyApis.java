package io.github.zjay.plugin.quickrequest.view.component.tree.allApis;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import io.github.zjay.plugin.quickrequest.contributor.PythonRequestMappingContributor;
import io.github.zjay.plugin.quickrequest.model.ApiService;
import io.github.zjay.plugin.quickrequest.model.OtherRequestEntity;
import io.github.zjay.plugin.quickrequest.util.LanguageEnum;
import io.github.zjay.plugin.quickrequest.util.ReflectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class PyApis extends BaseApis{
    @Override
    public List<OtherRequestEntity> getResultList(Project myProject) {
        return PythonRequestMappingContributor.getResultList(myProject);
    }

    @Override
    public void setBaseName(ApiService apiService, PsiFile psiFile) {
        apiService.setLanguage(LanguageEnum.Python);
        String name = (String)ReflectUtils.invokeMethod(psiFile, "getName");
        apiService.setClassName(name);
        Object presentation = ReflectUtils.invokeMethod(psiFile, "getPresentation");
        if (presentation == null){
            return;
        }
        String locationString = (String)ReflectUtils.invokeMethod(presentation, "getLocationString");
        if(StringUtils.isNotBlank(locationString)){
            String temp = locationString.replaceAll("\\(", "").replaceAll("\\)", "");
            String[] split = temp.split("\\.");
            locationString = temp.replaceFirst(split[0], "");
            apiService.setPackageName(locationString);
            if(locationString.startsWith(".")){
                apiService.setPackageName(locationString.replaceFirst("\\.", ""));
            }
        }
    }
}

package io.github.zjay.plugin.quickrequest.view.component.tree.allApis;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.impl.PyFileImpl;
import io.github.zjay.plugin.quickrequest.contributor.PythonRequestMappingContributor;
import io.github.zjay.plugin.quickrequest.model.ApiService;
import io.github.zjay.plugin.quickrequest.model.OtherRequestEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class PyApis extends BaseApis{
    @Override
    public List<OtherRequestEntity> getResultList(Project myProject) {
        return PythonRequestMappingContributor.getResultList(myProject);
    }

    @Override
    public void setBaseName(ApiService apiService, PsiFile psiFile) {
        PyFileImpl containingFile = (PyFileImpl)psiFile;
        apiService.setClassName(containingFile.getName());
        ItemPresentation presentation = containingFile.getPresentation();
        if(presentation != null && StringUtils.isNotBlank(presentation.getLocationString())){
            String locationString = presentation.getLocationString();
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

package io.github.zjay.plugin.quickrequest.view.component.tree.allApis;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import io.github.zjay.plugin.quickrequest.model.ApiService;
import io.github.zjay.plugin.quickrequest.model.OtherRequestEntity;
import io.github.zjay.plugin.quickrequest.util.go.GoMethod;
import io.github.zjay.plugin.quickrequest.view.component.tree.NodeUtil;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseApis {

    public abstract List<OtherRequestEntity> getResultList(Project myProject);

    public abstract void setBaseName(ApiService apiService, PsiFile psiFile);

    public List<ApiService> getApis(List<String> moduleNameList, Project myProject) {
        List<OtherRequestEntity> resultList = getResultList(myProject);
        List<ApiService> apiServiceList = new LinkedList<>();
        for (OtherRequestEntity otherRequestEntity : resultList) {
            if(!NodeUtil.judgeModule(otherRequestEntity.getElement(), moduleNameList)){
                continue;
            }
            ApiService apiService = new ApiService();
            apiServiceList.add(apiService);
            List<ApiService.ApiMethod> apiMethodList = new LinkedList<>();
            apiService.setApiMethodList(apiMethodList);
            apiMethodList.add(new ApiService.ApiMethod(otherRequestEntity.getElement(), otherRequestEntity.getUrlPath(), "", otherRequestEntity.getMethod(), GoMethod.getMethodType(otherRequestEntity.getMethod())));
            PsiFile containingFile = otherRequestEntity.getElement().getContainingFile();
            setBaseName(apiService, containingFile);
            Module module = ModuleUtil.findModuleForFile(containingFile);
            if (module != null) {
                apiService.setModuleName(module.getName());
            }
        }
        List<ApiService> resultApiList = new LinkedList<>();
        Map<String, List<ApiService>> map = apiServiceList.stream().collect(Collectors.groupingBy(x -> x.getModuleName() + "-" + x.getPackageName() + "-" + x.getClassName()));
        Iterator<Map.Entry<String, List<ApiService>>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, List<ApiService>> next = iterator.next();
            List<ApiService> value = next.getValue();
            ApiService apiService = value.get(0);
            List<ApiService.ApiMethod> apiMethodList = new LinkedList<>();
            value.forEach(x->apiMethodList.addAll(x.getApiMethodList()));
            apiService.setApiMethodList(apiMethodList);
            resultApiList.add(apiService);
        }
        return resultApiList;
    }
}

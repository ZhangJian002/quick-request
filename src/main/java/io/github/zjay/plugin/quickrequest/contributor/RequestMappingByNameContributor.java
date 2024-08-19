/*
 * Copyright 2021 zjay(darzjay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.zjay.plugin.quickrequest.contributor;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import io.github.zjay.plugin.quickrequest.config.Constant;
import io.github.zjay.plugin.quickrequest.config.FastRequestComponent;
import io.github.zjay.plugin.quickrequest.generator.FastUrlGenerator;
import io.github.zjay.plugin.quickrequest.generator.impl.DubboMethodGenerator;
import io.github.zjay.plugin.quickrequest.generator.impl.JaxRsGenerator;
import io.github.zjay.plugin.quickrequest.generator.impl.SpringMethodUrlGenerator;
import io.github.zjay.plugin.quickrequest.model.FastRequestConfiguration;
import io.github.zjay.plugin.quickrequest.util.FrIconUtil;
import io.github.zjay.plugin.quickrequest.util.FrPsiUtil;
import io.github.zjay.plugin.quickrequest.generator.linemarker.DubboLineMarkerProvider;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;


public abstract class RequestMappingByNameContributor implements ChooseByNameContributor {
    SpringMethodUrlGenerator springMethodUrlGenerator = ApplicationManager.getApplication().getService(SpringMethodUrlGenerator.class);
    JaxRsGenerator jaxRsGenerator = ApplicationManager.getApplication().getService(JaxRsGenerator.class);

    DubboMethodGenerator dubboMethodGenerator = ApplicationManager.getApplication().getService(DubboMethodGenerator.class);

    abstract List<PsiAnnotation> getAnnotationSearchers(String annotationName, Project project);
    private List<RequestMappingItem> navigationItems = new ArrayList<>();
    @Override
    public String @NotNull [] getNames(Project project, boolean includeNonProjectItems) {
        navigationItems = Constant.SUPPORTED_ANNOTATIONS.stream().flatMap(annotation -> findRequestMappingItems(project, annotation).stream()).collect(Collectors.toList());
        return navigationItems.stream()
                .map(RequestMappingItem::getName).distinct().toArray(String[]::new);
    }




    @Override
    public NavigationItem @NotNull [] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
        return navigationItems.stream().filter(q -> q.getName().equals(name)).toArray(RequestMappingItem[]::new);
    }

    private List<RequestMappingItem> findRequestMappingItems(Project project, String annotationName) {
        List<PsiAnnotation> annotationSearchers = getAnnotationSearchers(annotationName, project);
        //restful request
        ArrayList<RequestMappingItem> requestList = annotationSearchers.stream().filter(q -> fetchAnnotatedPsiElement(q) != null)
                .map(this::mapItems)
                .collect(Collectors.toCollection(ArrayList::new));

        //dubbo
        annotationSearchers.stream().filter(x-> x != null && x.getParent() != null && Constant.DubboMethodConfig.exist(x.getQualifiedName()) && x.getParent().getParent() instanceof PsiClass
            ).forEach(psiAnnotation -> {
            PsiClass parent = (PsiClass) psiAnnotation.getParent().getParent();
            PsiMethod[] methods = parent.getMethods();
            for (PsiMethod method : methods) {
                if(!DubboLineMarkerProvider.judgeMethod(method)){
                    continue;
                }
                requestList.add(new RequestMappingItem(method,dubboMethodGenerator.getMethodRequestMappingUrl(method),"DUBBO"));
            }
        });
//        List<RequestMappingItem> interfaceList = requestList.stream().filter(x -> {
//            PsiElement psiElement = x.getPsiElement();
//            try {
//                PsiFile psiFile = psiElement.getContainingFile();
//                if (Objects.equals(psiElement.getClass().getCanonicalName(), "com.intellij.psi.impl.source.PsiJavaFileImpl")) {
//                    PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
//                    return psiJavaFile.getClasses()[0].isInterface();
//                }
//            } catch (Exception e) {
//                return false;
//            }
//            return false;
//        }).collect(Collectors.toList());
//        interfaceList.forEach(requestList::remove);
//        requestList.addAll(interfaceList);
        return requestList;

    }



    private RequestMappingItem mapItems(PsiAnnotation psiAnnotation){
        PsiMethod method = fetchAnnotatedPsiElement(psiAnnotation);
        Constant.FrameworkType frameworkType = FrPsiUtil.calcFrameworkType(method);
        FastUrlGenerator generator;
        if (frameworkType.equals(Constant.FrameworkType.SPRING)) {
            generator = springMethodUrlGenerator;
        } else if(frameworkType.equals(Constant.FrameworkType.JAX_RS)){
            generator = jaxRsGenerator;
        }else {
            generator = dubboMethodGenerator;
            return new RequestMappingItem(method,generator.getMethodRequestMappingUrl(method),"DUBBO");
        }
        String methodUrl = generator.getMethodRequestMappingUrl(method);
        String classUrl = generator.getClassRequestMappingUrl(method);
        String originUrl = classUrl + "/" + methodUrl;
        originUrl = (originUrl.startsWith("/") ? "" : "/") + originUrl.replace("//", "/");
        String methodType = generator.getMethodType(method);
        return new RequestMappingItem(method,originUrl,methodType);
    }


    private PsiMethod fetchAnnotatedPsiElement(PsiElement psiElement) {
        if(psiElement == null){
            return null;
        }
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;
        if (config.getNeedInterface() == null || !config.getNeedInterface()){
            try {
                PsiJavaFile containingFile = (PsiJavaFile) psiElement.getContainingFile();
                if (containingFile.getClasses()[0].isInterface()){
                    return null;
                }
            }catch (Exception e){
                //ignore
            }
        }
        PsiElement parent = psiElement.getParent();
        if (parent instanceof PsiMethod) return (PsiMethod)parent;
        return fetchAnnotatedPsiElement(parent);
    }
}

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

package io.github.zjay.plugin.fastrequest.contributor;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtilCore;
import io.github.zjay.plugin.fastrequest.config.Constant;
import io.github.zjay.plugin.fastrequest.generator.FastUrlGenerator;
import io.github.zjay.plugin.fastrequest.generator.impl.DubboMethodGenerator;
import io.github.zjay.plugin.fastrequest.generator.impl.JaxRsGenerator;
import io.github.zjay.plugin.fastrequest.generator.impl.SpringMethodUrlGenerator;
import io.github.zjay.plugin.fastrequest.util.FrPsiUtil;
import io.github.zjay.plugin.fastrequest.view.linemarker.DubboLineMarkerProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.asJava.classes.KtUltraLightMethod;
import org.jetbrains.kotlin.asJava.elements.KtLightMethod;
import org.jetbrains.kotlin.psi.KtNamedFunction;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
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
        List<RequestMappingItem> requestList = annotationSearchers.stream().filter(q -> fetchAnnotatedPsiElement(q) != null)
                .map(this::mapItems)
                .collect(Collectors.toList());

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
        PsiElement parent = psiElement.getParent();
        if (parent instanceof PsiMethod) return (PsiMethod)parent;
        return fetchAnnotatedPsiElement(parent);
    }
}

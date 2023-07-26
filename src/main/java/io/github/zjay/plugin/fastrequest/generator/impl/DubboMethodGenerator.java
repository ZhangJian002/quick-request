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

package io.github.zjay.plugin.fastrequest.generator.impl;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiMethodImpl;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocToken;
import io.github.zjay.plugin.fastrequest.config.FastRequestComponent;
import io.github.zjay.plugin.fastrequest.generator.FastUrlGenerator;
import io.github.zjay.plugin.fastrequest.model.FastRequestConfiguration;
import io.github.zjay.plugin.fastrequest.model.ParamGroup;
import io.github.zjay.plugin.fastrequest.model.ParamNameType;
import io.github.zjay.plugin.fastrequest.parse.DubboParamParse;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;

public class DubboMethodGenerator extends FastUrlGenerator {
    private FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
    private DubboParamParse dubboParamParse = new DubboParamParse();

    @Override
    public String generate(PsiElement psiElement) {
        ParamGroup paramGroup = config.getParamGroup();
        if (!(psiElement instanceof PsiMethod)) {
            return StringUtils.EMPTY;
        }
        //methodType
        String methodType = "DUBBO";
        PsiMethod psiMethod = (PsiMethod) psiElement;
//        List<ParamNameType> methodBodyParamList = getMethodBodyParamList(psiMethod);
        //bodyParam
        LinkedHashMap<String, Object> bodyParamMap = dubboParamParse.parseParam(config, psiMethod.getParameterList());

        String methodDescription = getMethodDescription(psiMethod);
        paramGroup.getBodyParamMap().clear();
        paramGroup.setRequestParamMap(bodyParamMap);
        paramGroup.setMethodType(methodType);
        paramGroup.setMethodDescription(methodDescription);
        PsiClass containingClass = ((PsiMethodImpl) psiElement).getContainingClass();
        paramGroup.setClassName(containingClass.getQualifiedName());
        PsiClass[] interfaces = containingClass.getInterfaces();
        if(interfaces.length > 0){
            paramGroup.setInterfaceName(interfaces[0].getQualifiedName());
            paramGroup.setOriginUrl(paramGroup.getInterfaceName() + "." +psiMethod.getName());
        }
        paramGroup.setMethod(psiMethod.getName());
        Module moduleForFile = ModuleUtil.findModuleForPsiElement(psiElement);
        if (moduleForFile != null) {
            String name = moduleForFile.getName();
            paramGroup.setModule(name);
        }

        return null;
    }

    @Override
    public String getMethodRequestMappingUrl(PsiMethod psiMethod) {
        PsiClass[] interfaces = psiMethod.getContainingClass().getInterfaces();
        if(interfaces.length > 0){
            return interfaces[0].getQualifiedName() + "." + psiMethod.getName();
        }
        return "";
    }

    @Override
    public String getClassRequestMappingUrl(PsiMethod psiMethod) {
        return null;
    }

    @Override
    public List<ParamNameType> getMethodUrlParamList(PsiMethod psiMethod) {
        return null;
    }

    @Override
    public List<ParamNameType> getMethodBodyParamList(PsiMethod psiMethod) {
        return null;
    }


    @Override
    public String getMethodDescription(PsiMethod psiMethod) {
        //优先获取swagger接口ApiOperation中的value，如果获取不到则获取javadoc
        PsiAnnotation annotation = psiMethod.getAnnotation("io.swagger.annotations.ApiOperation");
        if (annotation != null) {
            PsiAnnotationMemberValue descValue = annotation.findDeclaredAttributeValue("value");
            if (descValue != null) {
                return descValue.getText().replace("\"","");
            }
        } else {
            //javadoc中获取
            PsiDocComment docComment = psiMethod.getDocComment();
            StringBuilder commentStringBuilder = new StringBuilder();
            if(docComment != null){
                PsiElement[] descriptionElements = docComment.getDescriptionElements();
                for (PsiElement descriptionElement : descriptionElements) {
                    if(descriptionElement instanceof PsiDocToken){
                        commentStringBuilder.append(descriptionElement.getText());
                    }
                }
            }
            return commentStringBuilder.toString().trim();
        }
        return null;
    }

    @Override
    public String getMethodType(PsiMethod psiMethod) {
        return null;
    }
}

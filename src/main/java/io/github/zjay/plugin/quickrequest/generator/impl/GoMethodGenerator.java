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

package io.github.zjay.plugin.quickrequest.generator.impl;

import com.intellij.psi.*;
import io.github.zjay.plugin.quickrequest.config.FastRequestComponent;
import io.github.zjay.plugin.quickrequest.generator.NormalUrlGenerator;
import io.github.zjay.plugin.quickrequest.model.FastRequestConfiguration;
import io.github.zjay.plugin.quickrequest.model.ParamGroup;
import io.github.zjay.plugin.quickrequest.my.AnalysisPsi;
import io.github.zjay.plugin.quickrequest.generator.linemarker.GoLineMarkerProvider;
import org.apache.commons.collections.MapUtils;

import java.util.LinkedHashMap;

public class GoMethodGenerator extends NormalUrlGenerator {
    private final FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
    @Override
    public String generate(PsiElement psiElement, String method, Object parameters) {
        ParamGroup paramGroup = config.getParamGroup();
        if(paramGroup.getBodyParamMap() != null){
            paramGroup.getBodyParamMap().clear();
        }
        paramGroup.setMethodType(method);
        PsiFile containingFile = psiElement.getContainingFile();
        paramGroup.setClassName(containingFile.getName().split("\\.")[0]);
        String url = GoLineMarkerProvider.getUrlFromPsi(psiElement);
        paramGroup.setMethod(url);
        paramGroup.setModule(psiElement.getProject().getName());
        paramGroup.setOriginUrl(url);
        paramGroup.setType(1);
        paramGroup.setMethodDescription(url);
        if(parameters != null && parameters instanceof PsiElement){
            LinkedHashMap<String, Object> paramsLinkedHashMap = AnalysisPsi.analysisPsi((PsiElement) parameters);
            if(MapUtils.isNotEmpty(paramsLinkedHashMap)){
                paramGroup.setBodyParamMap(paramsLinkedHashMap);
            }
        }
        return null;
    }

}

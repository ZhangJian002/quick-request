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

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.github.zjay.plugin.quickrequest.analysis.go.AnalysisPsi;
import io.github.zjay.plugin.quickrequest.config.FastRequestComponent;
import io.github.zjay.plugin.quickrequest.generator.NormalUrlGenerator;
import io.github.zjay.plugin.quickrequest.generator.linemarker.ProtobufLineMarkerProvider;
import io.github.zjay.plugin.quickrequest.generator.linemarker.RustLineMarkerProvider;
import io.github.zjay.plugin.quickrequest.model.FastRequestConfiguration;
import io.github.zjay.plugin.quickrequest.model.ParamGroup;
import io.github.zjay.plugin.quickrequest.util.ReflectUtils;
import org.apache.commons.collections.MapUtils;

import java.util.LinkedHashMap;
import java.util.List;

public class PbMethodGenerator extends NormalUrlGenerator {
    private FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
    @Override
    public String generate(PsiElement psiElement, String method, Object parameters) {
        ParamGroup paramGroup = config.getParamGroup();
        //methodType
        if (paramGroup.getBodyParamMap() == null){
            paramGroup.setBodyParamMap(new LinkedHashMap<>());
        }
        paramGroup.getBodyParamMap().clear();
        String[] url = ProtobufLineMarkerProvider.getUrl(psiElement);
        paramGroup.setMethodType("GRPC");
        assert url != null;
        paramGroup.setMethod(url[1]);
        paramGroup.setClassName(url[0]);
        paramGroup.setModule(psiElement.getProject().getName());
        paramGroup.setOriginUrl(url[0] + "/" + url[1]);
        paramGroup.setUrl(paramGroup.getOriginUrl());
        paramGroup.setType(7);
        PsiFile containingFile = psiElement.getContainingFile();
        String parent = "";
        if (containingFile.getVirtualFile() != null && containingFile.getVirtualFile().getParent() != null){
            parent = containingFile.getVirtualFile().getParent().getPath();
        }
        paramGroup.setPbInfo(parent, containingFile.getName());
        paramGroup.setBodyParamMap(ProtobufLineMarkerProvider.generateParams(psiElement));
        return null;
    }
}

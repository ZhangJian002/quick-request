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

import com.alibaba.fastjson.JSON;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.github.zjay.plugin.quickrequest.config.FastRequestComponent;
import io.github.zjay.plugin.quickrequest.generator.NormalUrlGenerator;
import io.github.zjay.plugin.quickrequest.generator.linemarker.RustLineMarkerProvider;
import io.github.zjay.plugin.quickrequest.model.FastRequestConfiguration;
import io.github.zjay.plugin.quickrequest.model.ParamGroup;

public class RustMethodGenerator extends NormalUrlGenerator {
    private FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
    @Override
    public String generate(PsiElement psiElement, String method, Object parameters) {
        ParamGroup paramGroup = config.getParamGroup();
        //methodType
        paramGroup.getBodyParamMap().clear();
        String[] methodType = RustLineMarkerProvider.getUrlAndMethodType(psiElement);
        assert methodType != null;
        paramGroup.setMethodType(methodType[1]);
        paramGroup.setMethod(method);
        PsiFile containingFile = psiElement.getContainingFile();
        paramGroup.setClassName(containingFile.getName().replaceFirst(".rs", ""));
        paramGroup.setModule(psiElement.getProject().getName());
        paramGroup.setOriginUrl(methodType[0]);
        paramGroup.setUrl(methodType[0]);
        paramGroup.setType(6);
        return null;
    }
}

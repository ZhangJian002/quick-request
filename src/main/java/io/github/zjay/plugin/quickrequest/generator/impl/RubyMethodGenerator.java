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
import io.github.zjay.plugin.quickrequest.config.FastRequestComponent;
import io.github.zjay.plugin.quickrequest.generator.NormalUrlGenerator;
import io.github.zjay.plugin.quickrequest.generator.linemarker.PyLineMarkerProvider;
import io.github.zjay.plugin.quickrequest.generator.linemarker.RubyLineMarkerProvider;
import io.github.zjay.plugin.quickrequest.model.FastRequestConfiguration;
import io.github.zjay.plugin.quickrequest.model.ParamGroup;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.methodCall.RCallImpl;

import java.util.LinkedHashMap;

public class RubyMethodGenerator extends NormalUrlGenerator {
    private FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
    @Override
    public String generate(PsiElement psiElement, String url, Object parameters) {
        ParamGroup paramGroup = config.getParamGroup();
        //methodType
        if (paramGroup.getBodyParamMap() == null){
            paramGroup.setBodyParamMap(new LinkedHashMap<>());
        }
        paramGroup.getBodyParamMap().clear();
        String[] methodType = RubyLineMarkerProvider.getUrlAndMethodType(psiElement);
        paramGroup.setMethodType(methodType[1]);
        paramGroup.setMethod(((RCallImpl)psiElement).getName());
        PsiFile containingFile = psiElement.getContainingFile();
        paramGroup.setClassName(containingFile.getName());
        paramGroup.setModule(psiElement.getProject().getName());
        paramGroup.setOriginUrl(url);
        paramGroup.setType(5);
        return null;
    }
}

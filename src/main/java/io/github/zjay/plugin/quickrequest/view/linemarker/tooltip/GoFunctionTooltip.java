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

package io.github.zjay.plugin.quickrequest.view.linemarker.tooltip;

import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import io.github.zjay.plugin.quickrequest.util.LanguageEnum;

public class GoFunctionTooltip extends BaseFunctionTooltip implements Function<PsiElement,String> {

    String url = "";

    PsiElement generateParams;
    public GoFunctionTooltip(PsiElement element, LanguageEnum language) {
        super(element, language);
    }

    public GoFunctionTooltip(PsiElement element, LanguageEnum language, String url, PsiElement generateParams) {
        this(element, language);
        this.url = url;
        this.generateParams = generateParams;
    }

    @Override
    public String fun(PsiElement psiElement) {
        return msg + url + " (or right-click to set)";
    }

    public PsiElement getGenerateParams(){
        return this.generateParams;
    }
}
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

package io.github.zjay.plugin.fastrequest.view.linemarker;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.util.Function;

public class FunctionTooltip implements Function<PsiElement,String> {

    String msg = "Generate Request For ";
    PsiElement element;

    public FunctionTooltip() {
    }

    public FunctionTooltip(PsiElement element) {
        this.element = element;
    }


    @Override
    public String fun(PsiElement psiElement) {
        PsiMethod psiMethod = (PsiMethod) psiElement.getParent();
        return msg + psiMethod.getName() + "(or right-click to set)";
    }
}
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

package io.github.zjay.plugin.quickrequest.util;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiMethod;
import io.github.zjay.plugin.quickrequest.config.Constant;

public class FrPsiUtil {

    public static Constant.FrameworkType calcFrameworkType(PsiMethod psiMethod) {
        PsiAnnotation jaxRsAnno = psiMethod.getAnnotation(Constant.JaxRsMappingConfig.PATH.getCode());
        if (jaxRsAnno != null) {
            return Constant.FrameworkType.JAX_RS;
        } else {
            for (Constant.SpringMappingConfig value : Constant.SpringMappingConfig.values()) {
                PsiAnnotation springAnno = psiMethod.getAnnotation(value.getCode());
                if(springAnno != null){
                    return Constant.FrameworkType.SPRING;
                }
            }
            return Constant.FrameworkType.DUBBO;
        }
    }
}

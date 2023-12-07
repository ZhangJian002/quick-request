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

package io.github.zjay.plugin.quickrequest.service;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import io.github.zjay.plugin.quickrequest.config.Constant;
import io.github.zjay.plugin.quickrequest.generator.FastUrlGenerator;
import io.github.zjay.plugin.quickrequest.generator.impl.DubboMethodGenerator;
import io.github.zjay.plugin.quickrequest.generator.impl.JaxRsGenerator;
import io.github.zjay.plugin.quickrequest.generator.impl.SpringMethodUrlGenerator;
import org.apache.commons.lang3.StringUtils;

public class GeneratorUrlService {
    SpringMethodUrlGenerator springMethodUrlGenerator = new SpringMethodUrlGenerator();
    JaxRsGenerator jaxRsGenerator = new JaxRsGenerator();

    DubboMethodGenerator dubboMethodGenerator = new DubboMethodGenerator();

    public String generate(PsiElement psiElement) {
        FastUrlGenerator fastUrlGenerator;
        if (!(psiElement instanceof PsiMethod)) {
            return StringUtils.EMPTY;
        }
        PsiMethod psiMethod = (PsiMethod) psiElement;
        String jaxPathAnnotation = Constant.JaxRsMappingConfig.PATH.getCode();
        PsiAnnotation annotation = psiMethod.getAnnotation(jaxPathAnnotation);
        if (annotation != null) {
            fastUrlGenerator = jaxRsGenerator;
        } else {
            Constant.SpringMappingConfig[] values = Constant.SpringMappingConfig.values();
            boolean isSpring = false;
            for (Constant.SpringMappingConfig value : values) {
                if(psiMethod.getAnnotation(value.getCode()) != null){
                    isSpring = true;
                    break;
                }
            }
            if(isSpring){
                fastUrlGenerator = springMethodUrlGenerator;
            }else {
                fastUrlGenerator = dubboMethodGenerator;
            }
        }
        return fastUrlGenerator.generate(psiElement);
    }
}

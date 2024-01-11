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

package io.github.zjay.plugin.quickrequest.contributor;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.zjay.plugin.quickrequest.config.Constant;
import io.github.zjay.plugin.quickrequest.generator.linemarker.PhpLineMarkerProvider;
import io.github.zjay.plugin.quickrequest.model.ApiService;
import io.github.zjay.plugin.quickrequest.model.OtherRequestEntity;
import io.github.zjay.plugin.quickrequest.util.php.PhpFirstFunction;
import io.github.zjay.plugin.quickrequest.util.php.PhpFunction;
import io.github.zjay.plugin.quickrequest.util.php.PhpLastFunction;
import io.github.zjay.plugin.quickrequest.util.PhpTwoJinZhi;
import io.github.zjay.plugin.quickrequest.util.TwoJinZhiGet;
import io.github.zjay.plugin.quickrequest.util.php.LaravelMethods;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class PhpRequestMappingContributor extends OtherRequestMappingByNameContributor{


    @Override
    List<OtherRequestEntity> getPsiElementSearchers(Project project) {
        List<OtherRequestEntity> resultList = new LinkedList<>();
        handlePhpPsiElement(TwoJinZhiGet.getRealStr(Constant.ROUTE), project, null, (target, apiService) -> {
            String[] result = getUrlAndMethodName(target);
            if(result != null && LaravelMethods.isExist(result[1])){
                resultList.add(new OtherRequestEntity(target.getFirstChild().getNextSibling().getNextSibling(), result[0], LaravelMethods.getMethodType(result[1])));
            }
        }, (apiService) -> {});
        return resultList;
    }

    public static void handlePhpPsiElement(String referenceName, Project project, PhpFirstFunction phpFirstFunction, PhpFunction phpFunction, PhpLastFunction phpLastFunction) {
        Collection<PsiElement> elements = getPhpReferencePsiElement(referenceName, project);
        for (PsiElement element : elements) {
            //真的是Route路由
            if(isRealRoute(element)){
                ApiService apiService = null;
                if(phpFirstFunction != null){
                    apiService = phpFirstFunction.doIt(element);
                }
                PsiElement[] psiElements = PsiTreeUtil.collectElements(element.getContainingFile(), dd -> true);
                for (PsiElement psiElement : psiElements) {
                    if(judge(psiElement)){
                        //再次判断
                        phpFunction.doIt(psiElement, apiService);
                    }
                }
                phpLastFunction.doIt(apiService);
            }
        }
    }

    public static boolean isRealRoute(PsiElement element) {
        try {
            Method getTargetReference = element.getClass().getMethod(TwoJinZhiGet.getRealStr(PhpTwoJinZhi.getTargetReference));
            getTargetReference.setAccessible(true);
            Object targetReference = getTargetReference.invoke(element);
            Method getFQN = targetReference.getClass().getMethod(TwoJinZhiGet.getRealStr(PhpTwoJinZhi.getFQN));
            getFQN.setAccessible(true);
            Object fqn = getFQN.invoke(targetReference);
            if(Objects.equals(TwoJinZhiGet.getRealStr(PhpTwoJinZhi.ROUTE),  fqn)){
                return true;
            }
        }catch (Exception e){

        }
        return false;
    }

    public static Collection<PsiElement> getPhpReferencePsiElement(String referenceName, Project project) {
        try {
            Class<?> aClass = Class.forName(TwoJinZhiGet.getRealStr(PhpTwoJinZhi.PhpUseReferenceNameIndex));
            Constructor<?> declaredConstructor = aClass.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            Object target = declaredConstructor.newInstance();
            Method getKey = aClass.getMethod(TwoJinZhiGet.getRealStr(PhpTwoJinZhi.getKey));
            getKey.setAccessible(true);
            StubIndexKey<String, PsiElement> key = (StubIndexKey<String, PsiElement>)getKey.invoke(target);
            return StubIndex.getElements(key, referenceName, project, GlobalSearchScope.projectScope(project), PsiElement.class);
        }catch (Exception e){

        }
        return new ArrayList<>();
    }

    public static String[] getUrlAndMethodName(PsiElement psiElement) {
        try {
            //请求方式
            Method getParameters = psiElement.getClass().getMethod(TwoJinZhiGet.getRealStr(PhpTwoJinZhi.getParameters));
            Object[] parameters = (Object[])getParameters.invoke(psiElement);
            String url = PhpLineMarkerProvider.getString((PsiElement) parameters[0]);
            Method getMethodName = psiElement.getClass().getMethod(TwoJinZhiGet.getRealStr(PhpTwoJinZhi.getName));
            String methodName = getMethodName.invoke(psiElement).toString();
            return new String[]{url, methodName};
        }catch (Exception e){

        }
        return null;
    }

    public static boolean judge(PsiElement psiElement) {
        try {
            if(Objects.equals(psiElement.getClass().getCanonicalName(), TwoJinZhiGet.getRealStr(PhpTwoJinZhi.MethodReferenceImpl))){
                Method getClassReference = psiElement.getClass().getMethod(TwoJinZhiGet.getRealStr(PhpTwoJinZhi.getClassReference));
                Object classReference = getClassReference.invoke(psiElement);
                Method getName = classReference.getClass().getMethod(TwoJinZhiGet.getRealStr(PhpTwoJinZhi.getName));
                Object name = getName.invoke(classReference);
                if(Objects.equals(name, TwoJinZhiGet.getRealStr(PhpTwoJinZhi.Route))){
                    return true;
                }
            }
        }catch (Exception e){

        }
        return false;
    }


}

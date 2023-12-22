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
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.kotlin.asJava.LightClassUtilsKt;
import org.jetbrains.kotlin.idea.stubindex.KotlinAnnotationsIndex;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class KotlinRequestMappingContributor extends RequestMappingByNameContributor{

    private static KotlinAnnotationsIndex kotlinAnnotationsIndex;

    static {
        try {
            Constructor<KotlinAnnotationsIndex> declaredConstructor = KotlinAnnotationsIndex.class.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            kotlinAnnotationsIndex = declaredConstructor.newInstance();
        } catch (Exception e) {

        }
    }

    @Override
    List<PsiAnnotation> getAnnotationSearchers(String annotationName, Project project) {
        //Kotlin
        if(kotlinAnnotationsIndex == null){
            return new ArrayList<>();
        }
        Collection<KtAnnotationEntry> ktAnnotationEntries = StubIndex.getElements(kotlinAnnotationsIndex.getKey(), annotationName, project, GlobalSearchScope.everythingScope(project), KtAnnotationEntry.class);
        return ktAnnotationEntries.stream().map(LightClassUtilsKt::toLightAnnotation).collect(Collectors.toList());
    }



}

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

package io.github.zjay.plugin.fastrequest.contributor;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.impl.java.stubs.index.JavaStubIndexKeys;
import com.intellij.psi.impl.search.JavaSourceFilterScope;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import kotlin.reflect.KClass;
import kotlin.reflect.KFunction;
import org.jetbrains.kotlin.asJava.LightClassUtilsKt;
import org.jetbrains.kotlin.idea.stubindex.KotlinAnnotationsIndex;
import org.jetbrains.kotlin.idea.stubindex.KotlinSourceFilterScope;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;
import org.jetbrains.kotlin.psi.KtElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class JavaRequestMappingContributor extends RequestMappingByNameContributor{


    @Override
    List<PsiAnnotation> getAnnotationSearchers(String annotationName, Project project) {
        //Java
        ArrayList<PsiAnnotation> javaAnnotations = new ArrayList<>(StubIndex.getElements(JavaStubIndexKeys.ANNOTATIONS, annotationName, project, new JavaSourceFilterScope(GlobalSearchScope.projectScope(project)), PsiAnnotation.class));
        List<PsiAnnotation> allAnnotations = new ArrayList<>(javaAnnotations);
        //Kotlin
        Collection<KtAnnotationEntry> ktAnnotationEntries =
                KotlinAnnotationsIndex.getInstance().get(annotationName, project, GlobalSearchScope.everythingScope(project));
        ktAnnotationEntries.stream().map(LightClassUtilsKt::toLightAnnotation).collect(Collectors.toList());
        ktAnnotationEntries.stream().forEach(ktAnnotationEntry -> {
            allAnnotations.add(LightClassUtilsKt.toLightAnnotation(ktAnnotationEntry));
        });
        return allAnnotations;
    }



}

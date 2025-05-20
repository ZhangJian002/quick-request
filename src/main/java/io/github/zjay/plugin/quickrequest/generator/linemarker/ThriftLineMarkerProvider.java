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

package io.github.zjay.plugin.quickrequest.generator.linemarker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import io.github.zjay.plugin.quickrequest.configurable.MyLineMarkerInfo;
import io.github.zjay.plugin.quickrequest.generator.linemarker.tooltip.GraphQLFunctionTooltip;
import io.github.zjay.plugin.quickrequest.grapyQL.GraphQLDataTypeEnum;
import io.github.zjay.plugin.quickrequest.grapyQL.GraphQLTypeEnum;
import io.github.zjay.plugin.quickrequest.grpc.proto.BuiltInTypeEnum;
import io.github.zjay.plugin.quickrequest.util.LanguageEnum;
import io.github.zjay.plugin.quickrequest.util.ReflectUtils;
import io.github.zjay.plugin.quickrequest.util.ToolWindowUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickRequest.icons.PluginIcons;

import java.util.*;

public class ThriftLineMarkerProvider implements LineMarkerProvider {

    private static final Logger log = LoggerFactory.getLogger(ThriftLineMarkerProvider.class);

    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        if (Objects.equals(element.getClass().getCanonicalName(), "com.intellij.plugins.thrift.lang.psi.impl.ThriftFunctionImpl")){

            List getFieldList = (List)ReflectUtils.invokeMethod(element, "getFieldList");
            for (Object field : getFieldList) {
                System.out.println(field);
                Object getFieldType = ReflectUtils.invokeMethod(field, "getFieldType");
                Object getCustomType = ReflectUtils.invokeMethod(getFieldType, "getCustomType");
                Object getBaseType = ReflectUtils.invokeMethod(getFieldType, "getBaseType");
                System.out.println(getCustomType + "" +  getBaseType);
                PsiReference[] getReferences = (PsiReference[])ReflectUtils.invokeMethod(getCustomType, "getReferences");
                System.out.println(getReferences);
                if (getReferences != null){
                    PsiReference firstReference = Arrays.stream(getReferences).filter(x -> Objects.equals(x.getClass().getCanonicalName(), "com.intellij.plugins.thrift.lang.psi.ThriftTypeReference")).findFirst().get();
                    if (firstReference != null){
                        PsiElement resolve = (PsiElement) ReflectUtils.invokeMethod(firstReference, "resolve");
                        PsiElement parent = resolve.getParent();
                        System.out.println(parent);
                        Object getFields = ReflectUtils.invokeMethod(parent, "getFields");
                        Object getFieldList1 = ReflectUtils.invokeMethod(getFields, "getFieldList");
                        System.out.println(getFieldList1);
                    }
                }
            }
            String url;
            if (!judgeRequest(element) || (url = getUrl(element)) == null){
                return null;
            }
            return new MyLineMarkerInfo<>(element, element.getTextRange(), PluginIcons.fastRequest_editor,
                    new GraphQLFunctionTooltip(element, LanguageEnum.GraphQL, url, null),
                    (e, elt) -> {
                        Project project = elt.getProject();
//                        ApplicationManager.getApplication().getService(GraphQLMethodGenerator.class).generate(element, null, null);
//                        ToolWindowUtil.openToolWindow(project);
//                        ToolWindowUtil.sendRequest(project, false);
                    },
                    GutterIconRenderer.Alignment.LEFT, () -> "quickRequest");
        }
        return null;
    }

    private boolean judgeRequest(PsiElement element) {
        try {
            Object child = ReflectUtils.invokeMethod(element, "getParent");
            Object getType = ReflectUtils.invokeMethod(child, "getParent");
            Object getTypeNameDefinition = ReflectUtils.invokeMethod(getType, "getTypeNameDefinition");
            Object getNameIdentifier = ReflectUtils.invokeMethod(getTypeNameDefinition, "getNameIdentifier");
            Object getReferenceName = ReflectUtils.invokeMethod(getNameIdentifier, "getReferenceName");
            return GraphQLTypeEnum.isExist((String)getReferenceName);
        }catch (Exception e){

        }
        return false;
    }

    public static LinkedHashMap<String, Object> generateParams(PsiElement psiElement){
        try {
            List getDirectives = (List)ReflectUtils.invokeMethod(psiElement, "getDirectives");
            for (Object getDirective : getDirectives) {
                Object getArguments = ReflectUtils.invokeMethod(getDirective, "getArguments");
                List getArgumentList = (List)ReflectUtils.invokeMethod(getArguments, "getArgumentList");
                for (Object getArgument : getArgumentList) {
                    Object getNameIdentifier = ReflectUtils.invokeMethod(getArgument, "getNameIdentifier");
                    Object getValue = ReflectUtils.invokeMethod(getArgument, "getValue");
                    System.out.println(getNameIdentifier + ":" + getValue);
                }
            }
            Class<?> aClass = Class.forName("com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryManager");
            Object getInstance = ReflectUtils.invokeStaticMethod(aClass, "getInstance", Project.class, psiElement.getProject());
            System.out.println(getInstance);

//            Collection<GoFunctionDeclaration> collection = StubIndex.getElements(GraphQLIdentifierIndex.NAME, TwoJinZhiGet.getRealStr(Constant.MAIN), psiElement.getProject(), GlobalSearchScope.projectScope(psiElement.getProject()), GraphQLIdentifierIndex.IdentifierKind.class);


            Object getArgumentsDefinition = ReflectUtils.invokeMethod(psiElement, "getArgumentsDefinition");
            List getInputValueDefinitionList = (List)ReflectUtils.invokeMethod(getArgumentsDefinition, "getInputValueDefinitionList");
            for (Object arg : getInputValueDefinitionList) {
                Object getType = ReflectUtils.invokeMethod(arg, "getType");
                Object getType2 = ReflectUtils.invokeMethod(getType, "getType");
                PsiElement getNameIdentifier = (PsiElement)ReflectUtils.invokeMethod(getType2, "getNameIdentifier");
                if (GraphQLDataTypeEnum.isExist(getNameIdentifier.getText())){
                    //Base
                    System.out.println(11);
                }else {
//                    PsiElement resolve2 = GraphQLResolveUtil.resolve((PsiElement) getType2);
//                    System.out.println(resolve2);
//                    Object getReference = ReflectUtils.invokeMethod(getNameIdentifier, "getReference");
//                    Object resolve = ReflectUtils.invokeMethod(getReference, "resolve");
//                    Object getReference1 = ReflectUtils.invokeMethod(resolve, "getReference");
//                    Object resolve1 = ReflectUtils.invokeMethod(getReference1, "resolve");
//                    System.out.println(resolve1);

                }
                System.out.println(getNameIdentifier.getText());
            }

            return null;
        }catch (Exception e){
            log.debug("handleMessageEntity error", e);
        }
        return new LinkedHashMap<>();
    }

    private static LinkedHashMap<String, Object> handleMessageEntity(Object target) {
        LinkedHashMap<String, Object> resultMap = new LinkedHashMap<>();
        Object getBody = ReflectUtils.invokeMethod(target, "getBody");
        List<Object> getSimpleFieldList = (List<Object>)ReflectUtils.invokeMethod(getBody, "getSimpleFieldList");
        List<Object> getMapFieldList = (List<Object>)ReflectUtils.invokeMethod(getBody, "getMapFieldList");
        List<Object> getOneofDefinitionList = (List<Object>)ReflectUtils.invokeMethod(getBody, "getOneofDefinitionList");
        List<Object> getEnumValueList = (List<Object>)ReflectUtils.invokeMethod(getBody, "getEnumValueList");

        if (getOneofDefinitionList != null){
            for (Object oneofDefinition : getOneofDefinitionList) {
                PsiElement oneofBody = (PsiElement)ReflectUtils.invokeMethod(oneofDefinition, "getBody");
                List<Object> oneofSimpleFieldList = (List<Object>)ReflectUtils.invokeMethod(oneofBody, "getSimpleFieldList");
                handleSimpleField(Arrays.asList(oneofSimpleFieldList.get(0)), resultMap,  target);
            }
        }

        handleSimpleField(getSimpleFieldList, resultMap, target);

        if (getMapFieldList != null){
            for (Object mapField : getMapFieldList) {
                PsiElement getNameIdentifier = (PsiElement)ReflectUtils.invokeMethod(mapField, "getNameIdentifier");
                Object getKeyType = ReflectUtils.invokeMethod(mapField, "getKeyType");
                Object getValueType = ReflectUtils.invokeMethod(mapField, "getValueType");
                resultMap.put(getNameIdentifier.getText(), getMapType(getKeyType, getValueType));
            }
        }

        if (getEnumValueList != null){
            Object firstEnum = getEnumValueList.get(0);
            PsiElement getNameIdentifier = (PsiElement)ReflectUtils.invokeMethod(firstEnum, "getNameIdentifier");
            resultMap.put(getNameIdentifier.getText(), getNameIdentifier.getText());
        }

        return resultMap;
    }

    private static void handleSimpleField(List<Object> getSimpleFieldList, LinkedHashMap<String, Object> resultMap, Object targetFile) {
        if (getSimpleFieldList == null)
            return;
        for (Object simpleField : getSimpleFieldList) {
            PsiElement getNameIdentifier = (PsiElement)ReflectUtils.invokeMethod(simpleField, "getNameIdentifier");
            Object getTypeName = ReflectUtils.invokeMethod(simpleField, "getTypeName");
            handleType(getNameIdentifier, getTypeName, resultMap, targetFile);
        }
    }

    private static void handleType(PsiElement getNameIdentifier, Object getTypeName, LinkedHashMap<String, Object> resultMap, Object targetFile) {
        Object value = getRandomValueFromType(getTypeName, getNameIdentifier.getText());
        resultMap.put(getNameIdentifier.getText(), value);
    }

    private static LinkedHashMap<Object, Object> getMapType(Object getKeyType, Object getValueType) {
        LinkedHashMap<Object, Object> mapMap = new LinkedHashMap<>();
        Object key = getRandomValueFromType(getKeyType, "key");
        Object value = getRandomValueFromType(getValueType, "value");
        mapMap.put(key, value);
        return mapMap;
    }

    private static Object getRandomValueFromType(Object type, String name){
        Object value;
        Object getBuiltInType = ReflectUtils.invokeMethod(type, "getBuiltInType");
        if (getBuiltInType != null){
            Object getName = ReflectUtils.invokeMethod(getBuiltInType, "getName");
            value = BuiltInTypeEnum.getRandomValue(getName, name);
        }else {
            //有个循环引用的问题TODO
            value = analyzeReference(type);
        }
        return value;
    }

    private static Object analyzeReference(Object type) {
        Object value;
        Object getEffectiveReference = ReflectUtils.invokeMethod(type, "getEffectiveReference");
        Object resolve = ReflectUtils.invokeMethod(getEffectiveReference, "resolve");
        String packageName = "";
        if (resolve != null){
            packageName = ThriftLineMarkerProvider.getPackageName((PsiElement) resolve);
            PsiElement messageIdentifier = (PsiElement)ReflectUtils.invokeMethod(resolve, "getNameIdentifier");
            packageName += "." + messageIdentifier.getText();
        }
        boolean isStruct = Objects.equals(packageName, "google.protobuf.Struct");
        if (isStruct){
            LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
            map.put(BuiltInTypeEnum.getRandomValue("string", "key"), BuiltInTypeEnum.getRandomValue("string", "value"));
            value = map;
        }else {
            value = handleMessageEntity(resolve);
        }
        return value;
    }


    public static String getUrl(PsiElement element) {
        try {
            PsiElement getNameIdentifier = (PsiElement)ReflectUtils.invokeMethod(element, "getNameIdentifier");
            return getNameIdentifier.getText();
        }catch (Exception e){

        }
        return null;
    }

    public static String getPackageName(PsiElement psiElement) {
        try {
            PsiFile containingFile = psiElement.getContainingFile();
            Object getPackageStatement = ReflectUtils.invokeMethod(containingFile, "getPackageStatement");
            Object getPackageName = ReflectUtils.invokeMethod(getPackageStatement, "getPackageName");
            Object getQualifiedName = ReflectUtils.invokeMethod(getPackageName, "getQualifiedName");
            assert getQualifiedName != null;
            return getQualifiedName.toString();
        }catch (Exception e){

        }
        return "";
    }

}

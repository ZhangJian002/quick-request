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

package io.github.zjay.plugin.quickrequest.view.component.tree;

import com.goide.psi.GoFile;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.util.Query;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.python.psi.PyFile;
import io.github.zjay.plugin.quickrequest.config.Constant;
import io.github.zjay.plugin.quickrequest.contributor.GoRequestMappingContributor;
import io.github.zjay.plugin.quickrequest.contributor.PhpRequestMappingContributor;
import io.github.zjay.plugin.quickrequest.contributor.PythonRequestMappingContributor;
import io.github.zjay.plugin.quickrequest.generator.impl.DubboMethodGenerator;
import io.github.zjay.plugin.quickrequest.generator.impl.JaxRsGenerator;
import io.github.zjay.plugin.quickrequest.generator.impl.SpringMethodUrlGenerator;
import io.github.zjay.plugin.quickrequest.model.ApiService;
import io.github.zjay.plugin.quickrequest.model.OtherRequestEntity;
import io.github.zjay.plugin.quickrequest.util.FrPsiUtil;
import io.github.zjay.plugin.quickrequest.generator.linemarker.DubboLineMarkerProvider;
import io.github.zjay.plugin.quickrequest.util.LanguageEnum;
import io.github.zjay.plugin.quickrequest.util.TwoJinZhiGet;
import io.github.zjay.plugin.quickrequest.util.go.GoMethod;
import io.github.zjay.plugin.quickrequest.util.php.LaravelMethods;
import io.github.zjay.plugin.quickrequest.view.component.tree.allApis.GoApis;
import io.github.zjay.plugin.quickrequest.view.component.tree.allApis.PyApis;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.*;
import java.util.stream.Collectors;

public class NodeUtil {


    public static List<ApiService> getAllApiList(Collection<PsiClass> controller) {
        SpringMethodUrlGenerator springMethodUrlGenerator = ApplicationManager.getApplication().getService(SpringMethodUrlGenerator.class);
        JaxRsGenerator jaxRsGenerator = ApplicationManager.getApplication().getService(JaxRsGenerator.class);
        DubboMethodGenerator dubboMethodGenerator = ApplicationManager.getApplication().getService(DubboMethodGenerator.class);

        List<ApiService> apiServiceList = new ArrayList<>();
        for (PsiClass psiClass : controller) {
            Module module = ModuleUtil.findModuleForPsiElement(psiClass);
            if (module == null) {
                continue;
            }
            String moduleName = module.getName();
            String className = psiClass.getName();
            PsiMethod[] methods = psiClass.getMethods();
            List<ApiService.ApiMethod> apiMethodList = new ArrayList<>();
            for (PsiMethod method : methods) {
                Constant.FrameworkType frameworkType = FrPsiUtil.calcFrameworkType(method);
                if (frameworkType.equals(Constant.FrameworkType.SPRING)) {
                    for (Constant.SpringMappingConfig value : Constant.SpringMappingConfig.values()) {
                        if (method.getAnnotation(value.getCode()) != null) {
                            String methodDescription = springMethodUrlGenerator.getMethodDescription(method);
                            String name = method.getName();
                            String methodUrl = springMethodUrlGenerator.getMethodRequestMappingUrl(method);
                            String classUrl = springMethodUrlGenerator.getClassRequestMappingUrl(method);
                            String originUrl = classUrl + "/" + methodUrl;
                            originUrl = (originUrl.startsWith("/") ? "" : "/") + originUrl.replace("//", "/");
                            String methodType = springMethodUrlGenerator.getMethodType(method);
                            ApiService.ApiMethod apiMethod = new ApiService.ApiMethod(method, originUrl, methodDescription, name, methodType);
                            apiMethodList.add(apiMethod);
                            break;
                        }
                    }
                } else if(frameworkType.equals(Constant.FrameworkType.JAX_RS)){
                    for (Constant.JaxRsMappingConfig value : Constant.JaxRsMappingConfig.values()) {
                        if (method.getAnnotation(value.getCode()) != null) {
                            String methodDescription = jaxRsGenerator.getMethodDescription(method);
                            String name = method.getName();
                            String methodUrl = jaxRsGenerator.getMethodRequestMappingUrl(method);
                            String classUrl = jaxRsGenerator.getClassRequestMappingUrl(method);
                            String originUrl = classUrl + "/" + methodUrl;
                            originUrl = (originUrl.startsWith("/") ? "" : "/") + originUrl.replace("//", "/");
                            String methodType = jaxRsGenerator.getMethodType(method);
                            ApiService.ApiMethod apiMethod = new ApiService.ApiMethod(method, originUrl, methodDescription, name, methodType);
                            apiMethodList.add(apiMethod);
                            break;
                        }
                    }
                }else if(frameworkType.equals(Constant.FrameworkType.DUBBO)){
                    if(!DubboLineMarkerProvider.judgeMethod(method)){
                        continue;
                    }
                    String methodDescription = dubboMethodGenerator.getMethodDescription(method);
                    String name = method.getName();
//                    String methodUrl = dubboMethodGenerator.getMethodRequestMappingUrl(method);
                    ApiService.ApiMethod apiMethod = new ApiService.ApiMethod(method, name, methodDescription, name, "DUBBO");
                    apiMethodList.add(apiMethod);
                }
            }

            String packageName = getPackageName(psiClass);
            if (packageName == null) {
                packageName = "";
            }

            ApiService apiService = new ApiService(LanguageEnum.java, moduleName, packageName, className, apiMethodList);
            apiServiceList.add(apiService);
        }
        return apiServiceList;
    }

    public static LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, List<ApiService>>>> convertToMap(List<ApiService> apiServiceList) {
        LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, List<ApiService>>>> dataMap = apiServiceList.stream().filter(x->x.getModuleName() != null).collect(Collectors.groupingBy(ApiService::getModuleName,
                LinkedHashMap::new,
                Collectors.groupingBy(ApiService::getPackageName,
                        LinkedHashMap::new,
                        Collectors.groupingBy(ApiService::getClassName,
                                LinkedHashMap::new,
                                Collectors.toList())
                )));
        return dataMap;
    }

    private static PackageNode findChildren(@NotNull PackageNode node) {
        List<PackageNode> children = new ArrayList<>();
        List<ClassNode> classChildren = new ArrayList<>();
        Enumeration<TreeNode> enumeration = node.children();
        while (enumeration.hasMoreElements()) {
            TreeNode ele = enumeration.nextElement();
            if (ele instanceof PackageNode) {
                sortChildNode((BaseNode) ele);
                children.add((PackageNode) ele);
            }else if(ele instanceof ClassNode){
                classChildren.add((ClassNode) ele);
            }
        }
        return CollectionUtils.isNotEmpty(classChildren) || children.size() > 1 ? null : children.get(0);
    }

    public static void convertToRoot(DefaultMutableTreeNode root, LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, List<ApiService>>>> dataMap, List<String> selectMethodType) {
        List<ModuleNode> moduleNodeList = new ArrayList<>();
        for (Map.Entry<String, LinkedHashMap<String, LinkedHashMap<String, List<ApiService>>>> moduleEntry : dataMap.entrySet()) {
            String moduleName = moduleEntry.getKey();
            ModuleNode moduleNode = new ModuleNode(moduleName);
            LinkedHashMap<String, LinkedHashMap<String, List<ApiService>>> packageMap = moduleEntry.getValue();
            Map<String, PackageNode> packageNodeMap = new LinkedHashMap<>();
            for (Map.Entry<String, LinkedHashMap<String, List<ApiService>>> packageEntry : packageMap.entrySet()) {
                String packageName = packageEntry.getKey();
                LinkedHashMap<String, List<ApiService>> classMap = packageEntry.getValue();
                for (Map.Entry<String, List<ApiService>> classEntry : classMap.entrySet()) {
                    String className = classEntry.getKey();
                    ClassNode classNode = new ClassNode(className, classEntry.getValue().get(0).getLanguage());
                    for (ApiService apiService : classEntry.getValue()) {
                        List<ApiService.ApiMethod> apiMethodList = apiService.getApiMethodList();
                        List<ApiService.ApiMethod> filterMethodList = apiMethodList.stream().filter(q -> selectMethodType.contains(q.getMethodType())).collect(Collectors.toList());
                        filterMethodList.forEach(apiMethod -> classNode.add(new MethodNode(apiMethod)));
                    }
                    customPending(packageNodeMap, packageName).add(classNode);
                }
            }
            List<PackageNode> nodes = new ArrayList<>();
            packageNodeMap.forEach((key, rootNode) -> {
                if ("".equals(key)) {
                    return;
                }
                while (true) {
                    PackageNode packageNode = findChildren(rootNode);
                    if (packageNode != null) {
                        rootNode.remove(packageNode);
                        String value = rootNode.getSource() + "." + packageNode.getSource();
                        packageNode.setSource(value);
                        packageNode.setUserObject(value);
                        rootNode = packageNode;
                    } else {
                        break;
                    }
                }
                sortChildNode(rootNode);
                nodes.add(rootNode);
            });
            nodes.forEach(moduleNode::add);
            PackageNode noPackageNode = packageNodeMap.get("");
            if (noPackageNode != null) {
                ArrayList<ClassNode> nodeList = (ArrayList<ClassNode>) IteratorUtils.toList(noPackageNode.children().asIterator());
                nodeList.sort(Comparator.comparing(ClassNode::toString));
                nodeList.forEach(moduleNode::add);
            }
            moduleNodeList.add(moduleNode);
        }
        moduleNodeList.sort(Comparator.comparing(ModuleNode::toString));
        moduleNodeList.forEach(root::add);
    }

    private static void sortChildNode(BaseNode rootNode) {
        ArrayList<BaseNode> nodeList = (ArrayList<BaseNode>) IteratorUtils.toList(rootNode.children().asIterator());
        nodeList.sort((n1, n2) -> {
            String prefix1 = n1 instanceof ClassNode ? "999999" : "000000";
            String prefix2 = n2 instanceof ClassNode ? "999999" : "000000";
            String name1 = n1.toString();
            String name2 = n2.toString();
            return (prefix1 + name1).compareTo(prefix2 + name2);
        });
        rootNode.removeAllChildren();
        nodeList.forEach(rootNode::add);
    }

    private static void sortPackage(List<PackageNode> list) {
        list.sort((p1, p2) -> {
            //有package优先,无package排最后 再按name排序
            int childCount1 = p1.getChildCount();
            int childCount2 = p2.getChildCount();
            String prefix1 = childCount1 == 0 ? "999999" : "000000";
            String prefix2 = childCount2 == 0 ? "999999" : "000000";
            String name1 = p1.toString();
            String name2 = p2.toString();
            return (prefix1 + name1).compareTo(prefix2 + name2);
        });
    }

    private static PackageNode customPending(@NotNull Map<String, PackageNode> data, @NotNull String packageName) {
        String[] names = packageName.split("\\.");
        PackageNode curr = data.computeIfAbsent(names[0], PackageNode::new);
        int fex = 1;
        while (fex < names.length) {
            String name = names[fex++];
            curr = findChild(curr, name);
        }
        return curr;
    }

    @NotNull
    private static PackageNode findChild(@NotNull PackageNode node, @NotNull String name) {
        Enumeration<TreeNode> children = node.children();
        while (children.hasMoreElements()) {
            TreeNode child = children.nextElement();
            if (!(child instanceof PackageNode)) {
                continue;
            }
            PackageNode packageNode = (PackageNode) child;
            if (name.equals(packageNode.getSource())) {
                return packageNode;
            }
        }
        PackageNode packageNode = new PackageNode(name);
        node.add(packageNode);
        return packageNode;
    }


    private static String getPackageName(@NotNull PsiClass psiClass) {
        String qualifiedName = psiClass.getQualifiedName();
        if (qualifiedName == null) {
            return null;
        }

        String fileName = psiClass.getName();
        if (fileName == null) {
            return null;
        }

        if (!qualifiedName.equals(fileName)) {
            return qualifiedName.substring(0, qualifiedName.lastIndexOf('.'));
        }

        return null;
    }

    public static List<ApiService> getJavaApis(List<String> moduleNameList, Project myProject) {
        try {
            Class<?> aClass = Class.forName("com.intellij.psi.search.searches.AllClassesSearch");
            if(aClass != null){
                Query<PsiClass> query = AllClassesSearch.search(ProjectScope.getContentScope(myProject), myProject);
                Collection<PsiClass> controller = query.findAll().stream().filter(cls -> cls.getAnnotation("org.springframework.web.bind.annotation.RestController") != null ||
                                cls.getAnnotation("org.springframework.stereotype.Controller") != null
                                || cls.getAnnotation("org.springframework.web.bind.annotation.RequestMapping") != null
                                || cls.getAnnotation(Constant.DubboMethodConfig.ApacheService.getCode()) != null
                                || cls.getAnnotation(Constant.DubboMethodConfig.DubboService.getCode()) != null
                                || cls.getAnnotation(Constant.DubboMethodConfig.AliService.getCode()) != null
                        )
                        .filter(
                                cls -> judgeModule(cls, moduleNameList)
                        ).collect(Collectors.toList());
                return NodeUtil.getAllApiList(controller);
            }
        }catch (Exception e){

        }
        return null;
    }

    public static List<ApiService> getPhpApis(List<String> moduleNameList, Project myProject) {
        //所有route引用，然后根据引用找到文件，再遍历
        List<ApiService> apiServiceList = new ArrayList<>();
        PhpRequestMappingContributor.handlePhpPsiElement(TwoJinZhiGet.getRealStr(Constant.ROUTE), myProject, psiElement -> {
            ApiService apiService = new ApiService();
            List<ApiService.ApiMethod> apiMethodList = new LinkedList<>();
            apiService.setApiMethodList(apiMethodList);
            return apiService;
        },(target, apiService) -> {
            if(judgeModule(target, moduleNameList)){
                String[] result = PhpRequestMappingContributor.getUrlAndMethodName(target);
                if(result != null && LaravelMethods.isExist(result[1])){
                    ApiService.ApiMethod apiMethod = new ApiService.ApiMethod(target.getFirstChild().getNextSibling().getNextSibling()
                            , result[0], "", result[1], LaravelMethods.getMethodType(result[1]));
                    apiService.getApiMethodList().add(apiMethod);
                }
            }
        }, (apiService) -> {
            if(CollectionUtils.isNotEmpty(apiService.getApiMethodList())){
                apiServiceList.add(apiService);
                apiService.setLanguage(LanguageEnum.php);
                PsiElement psiMethod = apiService.getApiMethodList().get(0).getPsiMethod();
                PhpFile containingFile = (PhpFile) psiMethod.getContainingFile();
                apiService.setPackageName(containingFile.getMainNamespaceName());
                apiService.setClassName(containingFile.getName());
                Module module = ModuleUtil.findModuleForFile(containingFile);
                if (module != null) {
                    apiService.setModuleName(module.getName());
                }
            }
        });
        return apiServiceList;
    }

    /**
     * 解析的api，如果没有特殊情况，都直接继承BaseApis实现其方法即可
     * @param moduleNameList
     * @param myProject
     * @return
     */
    public static List<ApiService> getGoApis(List<String> moduleNameList, Project myProject) {
        return new GoApis().getApis(moduleNameList, myProject);
    }

    /**
     * 解析的api，如果没有特殊情况，都直接继承BaseApis实现其方法即可
     * @param moduleNameList
     * @param myProject
     * @return
     */
    public static List<ApiService> getPythonApis(List<String> moduleNameList, Project myProject) {
        return new PyApis().getApis(moduleNameList, myProject);
    }

    public static boolean judgeModule(PsiElement cls, List<String> moduleNameList) {
        if (moduleNameList == null) {
            return true;
        }
        Module module = ModuleUtil.findModuleForFile(cls.getContainingFile());
        if (module == null) {
            return false;
        }
        return moduleNameList.contains(module.getName());
    }
}

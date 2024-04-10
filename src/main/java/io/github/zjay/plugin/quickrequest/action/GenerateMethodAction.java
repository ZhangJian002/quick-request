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

package io.github.zjay.plugin.quickrequest.action;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.CharFilter;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.ActiveIcon;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.ui.UIBundle;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.TextFieldCompletionProvider;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.textCompletion.TextFieldWithCompletion;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.JBUI;
import io.github.zjay.plugin.quickrequest.base.ParentAction;
import io.github.zjay.plugin.quickrequest.deprecated.MyComponentPanelBuilder;
import io.github.zjay.plugin.quickrequest.deprecated.MyPanelGridBuilder;
import io.github.zjay.plugin.quickrequest.util.KeywordUtil;
import io.github.zjay.plugin.quickrequest.util.MyResourceBundleUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quickRequest.icons.PluginIcons;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GenerateMethodAction extends ParentAction {

    private static final Pattern PATTERN = Pattern.compile("[a-zA-Z_$][a-zA-Z0-9_$]*");


    JBPopup popup;

    Editor editor;

    Project project;

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        project = anActionEvent.getData(LangDataKeys.PROJECT);
        if (project == null) {
            return;
        }
        // 获取当前的编辑器
        editor = anActionEvent.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        PsiJavaFile psiJavaFile = getPsiJavaFile();
        if(psiJavaFile == null){
            Messages.showMessageDialog("Please choose a java file!", UIBundle.message("error.dialog.title"), Messages.getErrorIcon());
            return;
        }
        if(!psiJavaFile.isWritable()){
            Messages.showMessageDialog("Please select an editable file!", UIBundle.message("error.dialog.title"), Messages.getErrorIcon());
            return;
        }
        if(psiJavaFile.getClasses()[0].isInterface() || psiJavaFile.getClasses()[0].isAnnotationType()){
            Messages.showMessageDialog("Please select a java file containing the class!", UIBundle.message("error.dialog.title"), Messages.getErrorIcon());
            return;
        }

        MyCustomForm form = new MyCustomForm(project);
        // 创建一个弹出窗口，并将表单窗口添加到其中
        popup = JBPopupFactory.getInstance().createComponentPopupBuilder(form.getMainPanel(), form.nameTextField)
                .setTitle(MyResourceBundleUtil.getKey("GenerateSpringMethod"))
                .setResizable(true)
                .setMovable(true)
                .setCancelOnClickOutside(false)
                .setFocusable(true)
                .setTitleIcon(new ActiveIcon(PluginIcons.fastRequest_toolwindow))
                .setRequestFocus(true)
                .createPopup();
        popup.showInBestPositionFor(editor);

    }

    private PsiJavaFile getPsiJavaFile() {
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        PsiFile psiFile = psiDocumentManager.getPsiFile(editor.getDocument());
        if(psiFile == null){
            return null;
        }
        if(!(psiFile instanceof PsiJavaFile)){
            return null;
        }
        return (PsiJavaFile)psiFile;
    }

    class MyCustomForm {
        private JPanel mainPanel;
        private JTextField nameTextField;
        private JButton generateButton;

        private JButton cancelButton;

        TextFieldWithCompletion returnTextField;

        ComboBox<String> methodTypes;

        JTextField urlTextField;

        JBCheckBox needGenerateSub;

        TextFieldWithCompletion serviceTextField;

        TextFieldWithCompletion paramTextField;

        Project project;

        private ClassComplete paramComplete;

        private ClassComplete returnComplete;

        private ClassComplete serviceComplete;

        private String methodfirstLine;

        List<PsiClass> realNeedImportList;

        List<PsiClass> realAutoImportList;

        public MyCustomForm(Project project) {
            this.project = project;
            this.realNeedImportList = new LinkedList<>();
            this.realAutoImportList = new LinkedList<>();
            buttonBindAction();
            buildPanel();
        }

        private void buildPanel() {
//            setNameTextField();
            nameTextField = new JTextField();
            nameTextField.setPreferredSize(new Dimension(500, 30));
            String[] methodType = {"Get", "Post", "Put", "Delete", "Other"};
            methodTypes = new ComboBox<>(methodType);
            urlTextField = new JTextField();
            paramTextField = new TextFieldWithCompletion(project, (paramComplete=new ClassComplete(project, true, 1)), "", true, true, true);
            paramComplete.setTextField(paramTextField);
            returnTextField = new TextFieldWithCompletion(project, (returnComplete=new ClassComplete(project, false, 2)), "", true, true, true);
            returnComplete.setTextField(returnTextField);
            serviceTextField = new TextFieldWithCompletion(project, (serviceComplete=new ClassComplete(project, false, 3)), "", true, true, true);
            serviceComplete.setTextField(serviceTextField);
            needGenerateSub = new JBCheckBox(MyResourceBundleUtil.getKey("GenerateSelected"), true);
            JPanel buttonJPanel = new JPanel(new GridBagLayout());
            buttonJPanel.add(generateButton);
            buttonJPanel.add(cancelButton);
            JPanel panel = new MyPanelGridBuilder()
                    .add(new MyComponentPanelBuilder(nameTextField).withLabel(MyResourceBundleUtil.getKey("MethodName")+":").withComment(MyResourceBundleUtil.getKey("MethodNameDes")))
                    .add(new MyComponentPanelBuilder(paramTextField).withLabel(MyResourceBundleUtil.getKey("Parameters")+":").withComment(MyResourceBundleUtil.getKey("ParametersDes")))
                    .add(new MyComponentPanelBuilder(methodTypes).withLabel(MyResourceBundleUtil.getKey("Type")+":").withComment(MyResourceBundleUtil.getKey("TypeDes")))
                    .add(new MyComponentPanelBuilder(urlTextField).withLabel(MyResourceBundleUtil.getKey("Url")+":").withComment(MyResourceBundleUtil.getKey("UrlDes")))
                    .add(new MyComponentPanelBuilder(returnTextField).withLabel(MyResourceBundleUtil.getKey("Return.class")+":").withComment(MyResourceBundleUtil.getKey("Return.classDes")))
                    .add(new MyComponentPanelBuilder(serviceTextField).withLabel(MyResourceBundleUtil.getKey("Bean.class")+":").withComment(MyResourceBundleUtil.getKey("Bean.classDes")))
                    .add(new MyComponentPanelBuilder(needGenerateSub).withComment(MyResourceBundleUtil.getKey("GenerateSelectedDes")))
                    .add(new MyComponentPanelBuilder(buttonJPanel).withTooltip(MyResourceBundleUtil.getKey("GenerateTooTip")))
                    .createPanel();
            mainPanel = new JPanel(new GridBagLayout());
            GridBag gb = new GridBag()
                    .setDefaultInsets(JBUI.insets(5, 10, 3, 10))
                    .setDefaultWeightX(1)
                    .setDefaultFill(GridBagConstraints.HORIZONTAL);
            mainPanel.add(panel, gb.nextLine().fillCell().weighty(1.0));

        }

        private void buttonBindAction() {
            generateButton = new JButton("Generate");
            cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> popup.cancel());
            generateButton.addActionListener(e -> {
                // 验证输入
                if(validInput()){
                    //生成方法
                    generateMethods();
                    //弹出关闭
                    popup.cancel();
                }
            });
        }

        private void generateMethods() {
            PsiJavaFile psiFile = getPsiJavaFile();
            if(psiFile == null){
                return;
            }
            WriteCommandAction.runWriteCommandAction(editor.getProject(), () -> {
                Caret currentCaret = editor.getCaretModel().getCurrentCaret();
                int offset = currentCaret.getOffset();
                // 创建 PsiElementFactory
                PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
                // 创建新的方法
                PsiMethod requestMethod = createRequestMethod(psiFile, elementFactory, offset);
                //创建导入语句
                createImports(psiFile, elementFactory);
                //接口+实现类 创建方法
                createSubClassMethods(elementFactory);
                // 移动光标到生成的方法的起始位置
                editor.getCaretModel().moveToOffset(requestMethod.getTextOffset());
            });
        }

        private void createSubClassMethods(PsiElementFactory elementFactory) {
            if(StringUtils.isBlank(serviceTextField.getText())){
                return;
            }
            List<PsiClass> serviceClasses = new LinkedList<>();
            serviceClasses.addAll(serviceComplete.getNeedImportList());
            serviceClasses.addAll(realAutoImportList);
            List<PsiClass> classes = serviceClasses.stream().filter(x -> x.getQualifiedName().endsWith(serviceTextField.getText().trim())).collect(Collectors.toList());
            if(classes.isEmpty()){
                return;
            }
            PsiClass psiClass = classes.get(0);
            if(!psiClass.getContainingFile().isWritable() || !needGenerateSub.isSelected()){
                return;
            }
            if(psiClass.isInterface()){
                //是接口
                PsiJavaFile containingFile = (PsiJavaFile) psiClass.getContainingFile();
                String methodForInteface = methodfirstLine.replaceFirst("public ", "").concat(";");
                importRealClass(psiClass, elementFactory, methodForInteface, containingFile);
                realAutoImportList.forEach(importClass -> {
                    PsiImportList importList = containingFile.getImportList();
                    if(importList == null){
                        PsiImportList.ARRAY_FACTORY.create(1);
                    }
                    //自动导入的类，由于不好判断导入哪一个，就只能按返回第一个来，但是如果已经存在名字一样的类了，就不导入了
                    long count = Arrays.stream(containingFile.getImportList().getImportStatements()).filter(x ->
                            x.getQualifiedName().endsWith(importClass.getName())
                                    || Objects.equals(psiClass.getQualifiedName(), importClass.getQualifiedName())
                    ).count();
                    if(count == 0){
                        PsiImportStatement importStatement = elementFactory.createImportStatement(importClass);
                        containingFile.getImportList().add(importStatement);
                    }
                });
                //所有的实现类
                Collection<PsiClass> psiClasses = ClassInheritorsSearch.search(psiClass, GlobalSearchScope.projectScope(project), true).findAll();
                for (PsiClass subPsiClass : psiClasses) {
                    //逐一生成
                    generateMethodForSubClass(subPsiClass, elementFactory, true);
                }
            }else {
                //直接生成
                generateMethodForSubClass(psiClass, elementFactory, false);
            }
        }

        private void generateMethodForSubClass(PsiClass subPsiClass, PsiElementFactory elementFactory, boolean needOverride) {
            String overrideStr = "";
            if(needOverride){
                overrideStr = "@Override\n";
            }
            StringBuilder sb = new StringBuilder(overrideStr);
            sb.append(methodfirstLine).append(" {\n");
            if(StringUtils.isNotBlank(returnTextField.getText())){
                sb.append("return null;");
            }
            sb.append("\n");
            sb.append("}");
            String implementMethod = sb.toString();
            PsiJavaFile subFile = (PsiJavaFile) subPsiClass.getContainingFile();
            importRealClass(subPsiClass, elementFactory, implementMethod, subFile);
            realAutoImportList.forEach(importClass -> {
                PsiImportList importList = subFile.getImportList();
                if(importList == null){
                    PsiImportList.ARRAY_FACTORY.create(1);
                }
                long count = Arrays.stream(subFile.getImportList().getImportStatements()).filter(x ->
                        x.getQualifiedName().endsWith(importClass.getQualifiedName())
                                || Objects.equals(subPsiClass.getQualifiedName(), importClass.getQualifiedName())
                ).count();
                if(count == 0){
                    PsiImportStatement importStatement = elementFactory.createImportStatement(importClass);
                    subFile.getImportList().add(importStatement);
                }
            });
        }

        private void importRealClass(PsiClass subPsiClass, PsiElementFactory elementFactory, String implementMethod, PsiJavaFile subFile) {
            PsiMethod implement = elementFactory.createMethodFromText(implementMethod, subFile);
            subPsiClass.add(implement);
            realNeedImportList.forEach(importClass -> {
                PsiImportList importList = subFile.getImportList();
                if(importList == null){
                    PsiImportList.ARRAY_FACTORY.create(1);
                }
                long count = Arrays.stream(subFile.getImportList().getImportStatements()).filter(x ->
                        Objects.equals(x.getQualifiedName(), importClass.getQualifiedName())
                                || Objects.equals(subPsiClass.getQualifiedName(), importClass.getQualifiedName())
                ).count();
                if(count == 0){
                    PsiImportStatement importStatement = elementFactory.createImportStatement(importClass);
                    subFile.getImportList().add(importStatement);
                }
            });
        }

        private void createImports(PsiJavaFile psiFile, PsiElementFactory elementFactory) {
            boolean isExit = false;
            PsiImportList importList = psiFile.getImportList();
            if (importList == null) {
                importList = PsiImportList.ARRAY_FACTORY.create(1)[0];
            }else {
                PsiImportStatement[] importStatements = importList.getImportStatements();
                for (PsiImportStatement importStatement : importStatements) {
                    if(Objects.equals(importStatement.getQualifiedName(), "org.springframework.web.bind.annotation.*")){
                        isExit = true;
                    }
                }
            }
            if(!isExit){
                PsiImportStatement importStatement = elementFactory.createImportStatementOnDemand("org.springframework.web.bind.annotation");
                importList.add(importStatement);
            }
            PsiImportList finalImportList = importList;
            realNeedImportList.forEach(psiClass -> {
                long count = Arrays.stream(finalImportList.getImportStatements()).filter(x -> Objects.equals(x.getQualifiedName(), psiClass.getQualifiedName())).count();
                if(count == 0){
                    PsiImportStatement importStatement = elementFactory.createImportStatement(psiClass);
                    finalImportList.add(importStatement);
                }
            });
            realAutoImportList.forEach(psiClass -> {
                long count = Arrays.stream(finalImportList.getImportStatements()).filter(x -> x.getQualifiedName().endsWith(psiClass.getName())).count();
                if(count == 0){
                    PsiImportStatement importStatement = elementFactory.createImportStatement(psiClass);
                    finalImportList.add(importStatement);
                }
            });
        }

        /**
         * 将param、return和service的bean class获得放入list
         */
        private void addImportListForField() {
            if(StringUtils.isNotBlank(paramTextField.getText())){
                List<PsiClass> needImportList = paramComplete.getNeedImportList();
                String[] params = paramTextField.getText().trim().split(",");
                for (String param : params) {
                    for (PsiClass importParam : needImportList) {
                        if(importParam.getName().contains(param)){
                            //need import
                            realNeedImportList.add(importParam);
                        }
                    }
                }
            }
            importClass(returnTextField, returnComplete);
            importClass(serviceTextField, serviceComplete);
        }

        private void importClass(TextFieldWithCompletion returnTextField, ClassComplete returnComplete) {
            if(StringUtils.isNotBlank(returnTextField.getText())){
                List<PsiClass> needImportList = returnComplete.getNeedImportList();
                String inner = getInner(returnTextField.getText().trim());
                if(inner == null || "".equals(inner)){
                    String param = returnTextField.getText().replace("<>", "").trim();
                    for (PsiClass importParam : needImportList) {
                        if(importParam.getName().contains(param)){
                            //need import
                            realNeedImportList.add(importParam);
                        }
                    }
                }else {
                    String trim = returnTextField.getText().trim();
                    String param = trim.substring(0, trim.indexOf("<"));
                    for (PsiClass importParam : needImportList) {
                        if(importParam.getName().contains(param) || importParam.getName().contains(inner)){
                            //need import
                            realNeedImportList.add(importParam);
                        }
                    }
                }

            }
        }

        private PsiMethod createRequestMethod(PsiJavaFile psiFile, PsiElementFactory elementFactory, int offset) {
            StringBuilder sb = new StringBuilder();
            String returnName = StringUtils.isBlank(returnTextField.getText()) ? "void" : returnTextField.getText().trim();
            String methodType = (String) methodTypes.getSelectedItem();
            if(StringUtils.isBlank(methodType) || "Other".equals(methodType)){
                methodType = "Request";
            }
            sb.append("@").append(methodType).append("Mapping(\"").append(urlTextField.getText()).append("\")\n");

            if(!Objects.equals("Get", methodType) && !Objects.equals("Request", methodType)){
                sb.append("@ResponseBody").append("\n");
            }
            String text = nameTextField.getText();
            StringBuilder methodFistLine = new StringBuilder();
            methodFistLine.append("public ").append(returnName).append(" ").append(text).append("(");
            String[] paramStr = getParamStr();
            methodFistLine.append(paramStr[0]).append(")");
            methodfirstLine = methodFistLine.toString();
            sb.append(methodFistLine);
            sb.append(" {\n");
            if(StringUtils.isBlank(serviceTextField.getText())){
                if(!Objects.equals("void", returnName)){
                    sb.append("\nreturn null;");
                }
            }else {
                PsiField[] fields = psiFile.getClasses()[0].getFields();
                String fieldName = "";
                for (PsiField field : fields) {
                    PsiType fieldType = field.getType();
                    String fieldTypeName = fieldType.getPresentableText();
                    if(Objects.equals(fieldTypeName, serviceTextField.getText().trim())){
                        //找到同一个属性了
                        fieldName = field.getName();
                        break;
                    }
                }
                if(StringUtils.isBlank(fieldName)){
                    fieldName = StringUtil.decapitalize(serviceTextField.getText());
                    PsiField fieldFromText = elementFactory.createFieldFromText("@Autowired\nprivate " + serviceTextField.getText() + " " + fieldName + ";", psiFile);
                    psiFile.getClasses()[0].add(fieldFromText);
                }
                if(StringUtils.isNotBlank(returnTextField.getText())){
                    sb.append("return ");
                }
                sb.append(fieldName).append(".").append(text.trim()).append("(");
                sb.append(paramStr[1]);
                sb.append(");");
            }
            sb.append("\n}");
            //从光标获取上下文方法
            PsiElement element = PsiUtilBase.getElementAtCaret(editor);
            PsiMethod prevMethod = PsiTreeUtil.getPrevSiblingOfType(element, PsiMethod.class);
            PsiMethod newMethod = elementFactory.createMethodFromText(sb.toString(), psiFile);
            PsiMethod finalMethod = null;
            if(prevMethod != null && prevMethod.getParent() == psiFile.getClasses()[0]){
                finalMethod = (PsiMethod)psiFile.getClasses()[0].addAfter(newMethod, prevMethod);
            }
            if(finalMethod == null){
                PsiMethod nextMethod = PsiTreeUtil.getNextSiblingOfType(element, PsiMethod.class);
                if(nextMethod != null && nextMethod.getParent() == psiFile.getClasses()[0]){
                    finalMethod = (PsiMethod)psiFile.getClasses()[0].addBefore(newMethod, nextMethod);
                }
            }
            if(finalMethod == null){
                finalMethod = (PsiMethod)psiFile.getClasses()[0].add(newMethod);
            }
            CodeStyleManager.getInstance(project).reformat(psiFile);
            return finalMethod;
        }

        private String[] getParamStr() {
            String[] result = new String[]{"",""};
            String paramText = paramTextField.getText();
            if(StringUtils.isNotBlank(paramText)){
                String[] params = paramText.trim().split(",");
                Map<String, Integer> paramsMap = new HashMap<>();
                for (String param : params) {
                    String trim = param.trim();
                    if(!paramsMap.containsKey(trim)){
                        paramsMap.put(trim, 1);
                    }else {
                        Integer integer = paramsMap.get(trim);
                        paramsMap.put(trim, integer+1);
                    }
                }
                List<String> paramList = new LinkedList<>();
                List<String> paramListTemp = new LinkedList<>();
                Iterator<Map.Entry<String, Integer>> iterator = paramsMap.entrySet().iterator();
                while (iterator.hasNext()){
                    Map.Entry<String, Integer> next = iterator.next();
                    if(next.getValue() == 1){
                        String decapitalize = StringUtil.decapitalize(next.getKey());
                        if(KeywordUtil.javaKeywords.contains(decapitalize)){
                            decapitalize = decapitalize + "0";
                        }
                        paramList.add(next.getKey() + " " + decapitalize);
                        paramListTemp.add(decapitalize);
                    }else {
                        for (int i = 0; i < next.getValue(); i++) {
                            String className = next.getKey() + (i+1);
                            String decapitalize = StringUtil.decapitalize(className);
                            paramList.add(next.getKey() + " " + decapitalize);
                            paramListTemp.add(decapitalize);
                        }
                    }
                }
                result[0] = StringUtils.join(paramList, ", ");
                result[1] = StringUtils.join(paramListTemp, ", ");
            }
            return result;
        }


        private boolean validInput() {
            //将参数、返回和bean的class添加入list
            addImportListForField();
            PsiJavaFile psiJavaFile = getPsiJavaFile();
            if(psiJavaFile == null){
                Messages.showMessageDialog("Please open a Java file!", UIBundle.message("error.dialog.title"), Messages.getErrorIcon());
                return false;
            }
            if(!psiJavaFile.isWritable()){
                Messages.showMessageDialog("The current file is read-only!", UIBundle.message("error.dialog.title"), Messages.getErrorIcon());
                return false;
            }
            String text = nameTextField.getText();
            if(StringUtils.isBlank(text)){
                Messages.showMessageDialog("Method name cannot be empty!", UIBundle.message("error.dialog.title"), Messages.getErrorIcon());
                return false;
            }
            if(!PATTERN.matcher(text.trim()).matches()){
                Messages.showMessageDialog("Method naming does not comply with regulations!", UIBundle.message("error.dialog.title"), Messages.getErrorIcon());
                return false;
            }
            String url = urlTextField.getText();
            if(StringUtils.isBlank(url)){
                Messages.showMessageDialog("Url cannot be empty!", UIBundle.message("error.dialog.title"), Messages.getErrorIcon());
                return false;
            }
            //校验输入的class是否符合要求
            if(StringUtils.isNotBlank(returnTextField.getText())){
                if(findClassName(returnTextField.getText())){
                    Messages.showMessageDialog("Return class [" + getRealText(returnTextField.getText()) + "] doesn't exist!", UIBundle.message("error.dialog.title"), Messages.getErrorIcon());
                    return false;
                }
            }
            if(StringUtils.isNotBlank(serviceTextField.getText())){
                if(findClassName(serviceTextField.getText())){
                    Messages.showMessageDialog("Bean class [" + getRealText(serviceTextField.getText()) + "] doesn't exist!", UIBundle.message("error.dialog.title"), Messages.getErrorIcon());
                    return false;
                }
            }
            if(StringUtils.isNotBlank(paramTextField.getText())){
                String[] params = paramTextField.getText().split(",");
                for (String param : params) {
                    if(StringUtils.isNotBlank(param)){
                        if(findClassName(param)){
                            Messages.showMessageDialog("Parameter class [" + getRealText(param) + "] doesn't exist!", UIBundle.message("error.dialog.title"), Messages.getErrorIcon());
                            return false;
                        }
                    }
                }
            }

            return true;
        }

        private String getRealText(String param) {
            return param.replace("<", "&lt;").replace(">", "&gt;").trim();
        }

        private boolean findClassName(String text) {
            try {
                //getClassesByName这个方法必须被这样包裹住，不然会报各种慢操作问题
                return !AppExecutorUtil.getAppExecutorService().submit(() -> ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> {
                    String trim = text.trim();
                    String inner = getInner(trim);
                    if(inner == null || "".equals(inner)){
                        PsiClass[] psiClass = PsiShortNamesCache.getInstance(project).getClassesByName(trim.replace("<>", ""), GlobalSearchScope.allScope(project));
                        return handlePsiClass(psiClass);
                    }else {
                        PsiClass[] psiClass = PsiShortNamesCache.getInstance(project).getClassesByName(inner, GlobalSearchScope.allScope(project));
                        if(psiClass.length == 0){
                            return false;
                        }
                        handlePsiClass(psiClass);
                        PsiClass[] psiClass1 = PsiShortNamesCache.getInstance(project).getClassesByName(trim.substring(0, trim.indexOf("<")), GlobalSearchScope.allScope(project));
                        return handlePsiClass(psiClass1);
                    }
                })).get();
            } catch (Exception e) {
            }
            return false;
        }

        private Boolean handlePsiClass(PsiClass[] psiClass) {
            if(psiClass.length > 0){
                //有这个类 取第一个
                PsiClass targetClass = psiClass[0];
                //java.lang的不用导入
                if(!targetClass.getQualifiedName().startsWith("java.lang.")){
                    boolean existClass = false;
                    //判断realNeedImportList里面是否有这个名字的类
                    for (PsiClass item : realNeedImportList) {
                        if(Objects.equals(item.getName(), targetClass.getName())){
                            existClass = true;
                        }
                    }
                    if(!existClass){
                        //没有的话，代表是复制的或者没有选择的，加进来就好
                        realAutoImportList.add(targetClass);
                    }
                }
                return true;
            }
            return false;
        }

        private String getInner(String target){
            String trim = target.trim();
            int start = trim.indexOf("<");
            int end = trim.lastIndexOf(">");
            if(start != -1 && end != -1){
                return trim.substring(start+1, end).trim();

            }else {
                return null;
            }
        }

        public JPanel getMainPanel() {
            return mainPanel;
        }
    }



    class ClassComplete extends TextFieldCompletionProvider {
        private final Project project;

        private final List<PsiClass> needImportList;

        private final boolean many;

        private final int type;

        TextFieldWithCompletion textField;

        public List<PsiClass> getNeedImportList(){
            return needImportList;
        }

        public void setTextField(TextFieldWithCompletion textField){
            this.textField = textField;
        }

        public ClassComplete(Project project, boolean many, int type){
            this.project = project;
            this.many = many;
            this.needImportList = new LinkedList<>();
            this.type = type;
        }

        @Override
        public CharFilter.@Nullable Result acceptChar(char c) {
//            if(c == '<'){
//                this.textField.setText(this.textField.getText() + ">");
//            }
            return super.acceptChar(c);
        }

        @Override
        public @Nullable String getPrefix(@NotNull String text, int offset) {
            String substring = text.substring(0, offset);
            if(many){
                String[] split = substring.split(",");
                return split[split.length-1].trim();
            }else {
                //dddd
                if(substring.contains("<")){
                    return text.substring(text.indexOf("<")+1, offset);
                }
                return super.getPrefix(text, offset);
            }
        }


        @Override
        protected void addCompletionVariants(@NotNull String text, int offset, @NotNull String prefix, @NotNull CompletionResultSet result) {
            if(StringUtils.isBlank(text)){
                return;
            }
            String finalText = getFinalText(text, offset);
            // 使用模糊搜索查找匹配的类名
            Collection<PsiClass> psiClasses = AllClassesSearch.search(GlobalSearchScope.allScope(project), project, s -> s.contains(finalText)).findAll();
            // 将匹配的类名添加到自动补全结果集
            for (PsiClass psiClass : psiClasses) {
                if(psiClass.isAnnotationType()){
                    //不要注解
                    continue;
                }
                PsiFile psiFile = psiClass.getContainingFile();
                String packageName;
                if (psiFile instanceof PsiJavaFile) {
                    PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                    packageName = psiJavaFile.getPackageName();
                }else {
                    packageName = psiClass.getQualifiedName();
                }
                packageName = " " + packageName;
                LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(psiClass)
                        .withIcon(psiClass.getIcon(Iconable.ICON_FLAG_READ_STATUS))
                        .withBoldness(true).withTailText(packageName, true).withInsertHandler((context, item) -> {
                            PsiClass psiClz = (PsiClass) item.getObject();
                            if(psiClz.getQualifiedName().startsWith("java.lang.")){
                                return;
                            }
                            //删除之前导入过得  防止重复
                            needImportList.removeIf(s -> Objects.nonNull(psiClz.getName()) && Objects.equals(s.getName(), psiClz.getName()));
                            needImportList.add(psiClz);
                        });
                result.addElement(lookupElementBuilder);
            }
        }

        @Override
        public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull String prefix, @NotNull CompletionResultSet result) {
            super.fillCompletionVariants(parameters, prefix, result);

        }

        private String getFinalText(String text, int offset) {
            int length = 0;
            if(many){
                String[] classes = text.split(",");
                for (int i = 0; i < classes.length; i++) {
                    length += classes[i].length() + 1;
                    if(offset <= length){
                        //在当前类下面
                        text = classes[i];
                        break;
                    }
                }
            }else {
                String substring = text.substring(0, offset);
                if(substring.contains("<")){
                    return text.substring(text.indexOf("<")+1, offset);
                }
            }
            return text;
        }
    }






}

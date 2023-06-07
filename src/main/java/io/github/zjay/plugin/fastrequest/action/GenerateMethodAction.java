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

package io.github.zjay.plugin.fastrequest.action;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.*;
import com.intellij.ide.actions.searcheverywhere.AutoCompletionCommand;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiClassPattern;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.PsiMemberPattern;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.java.stubs.index.JavaShortClassNameIndex;
import com.intellij.psi.impl.source.PsiImportListImpl;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import com.intellij.util.IconUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.TextFieldCompletionProvider;
import com.intellij.util.textCompletion.TextFieldWithCompletion;
import com.intellij.util.ui.JBUI;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateMethodAction extends AnAction {

    JBPopup popup;

    Editor editor;

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getData(LangDataKeys.PROJECT);
        if (project == null) {
            return;
        }
        // 获取当前的编辑器
        editor = anActionEvent.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        MyCustomForm form = new MyCustomForm(project);

        // 创建一个弹出窗口，并将表单窗口添加到其中
        popup = JBPopupFactory.getInstance().createComponentPopupBuilder(form.getMainPanel(), form.textField)
                .setTitle("Generate Spring Method")
                .setResizable(true)
                .setMovable(true)
                .setCancelOnClickOutside(false)
                .setFocusable(true)
                .setRequestFocus(true)
                .createPopup();
        popup.showInBestPositionFor(editor);

    }

    class MyCustomForm {
        private JPanel mainPanel;
        private JTextField textField;
        private JButton button;

        TextFieldWithCompletion returnTextField;

        ComboBox<String> methodTypes;

        JTextField urlTextField;

        TextFieldWithCompletion serviceTextField;

        TextFieldWithCompletion paramTextField;

        Project project;

        public MyCustomForm(Project project) {
            this.project = project;
            buttonBindAction();
            buildPanel();
        }

        private void buildPanel() {
            textField = new JTextField();
            textField.setPreferredSize(new Dimension(300, 30));
            JLabel label = new JLabel("Name:");
            mainPanel = new JPanel(new GridBagLayout());
            // 创建 GridBagConstraints 对象来设置组件的位置和约束
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0; // 列索引为 0
            gbc.gridy = 0; // 行索引为 0
            gbc.anchor = GridBagConstraints.LINE_START; // 组件对齐方式为左对齐
            gbc.insets = JBUI.insets(5); // 设置组件之间的间距
            mainPanel.add(label, gbc); // 将标签添加到面板

            gbc.gridx = 1; // 列索引为 1
            gbc.gridy = 0; // 行索引为 0
            gbc.fill = GridBagConstraints.HORIZONTAL; // 水平拉伸组件
            gbc.weightx = 1.0; // 设置组件在水平方向上的拉伸权重
            mainPanel.add(textField, gbc);

            JLabel typeLabel = new JLabel("Type:");
            String[] methodType = {"Get", "Post", "Put", "Delete", "Other"};
            methodTypes = new ComboBox<>(methodType);

            gbc.gridx = 0; // 列索引为 0
            gbc.gridy = 1; // 行索引为 0
            gbc.anchor = GridBagConstraints.LINE_START; // 组件对齐方式为左对齐
            gbc.insets = JBUI.insets(5); // 设置组件之间的间距
            mainPanel.add(typeLabel, gbc); // 将标签添加到面板

            gbc.gridx = 1; // 列索引为 1
            gbc.gridy = 1; // 行索引为 0
            gbc.fill = GridBagConstraints.HORIZONTAL; // 水平拉伸组件
            gbc.weightx = 1.0; // 设置组件在水平方向上的拉伸权重
            mainPanel.add(methodTypes, gbc);

            JLabel urlLabel = new JLabel("Url:");
            urlTextField = new JTextField();

            gbc.gridx = 0; // 列索引为 0
            gbc.gridy = 2; // 行索引为 0
            gbc.anchor = GridBagConstraints.LINE_START; // 组件对齐方式为左对齐
            gbc.insets = JBUI.insets(5); // 设置组件之间的间距
            mainPanel.add(urlLabel, gbc); // 将标签添加到面板

            gbc.gridx = 1; // 列索引为 1
            gbc.gridy = 2; // 行索引为 0
            gbc.fill = GridBagConstraints.HORIZONTAL; // 水平拉伸组件
            gbc.weightx = 1.0; // 设置组件在水平方向上的拉伸权重
            mainPanel.add(urlTextField, gbc);

            JLabel paramLabel = new JLabel("Parameters:");
            paramTextField = new TextFieldWithCompletion(project, new ClassComplete(project, true), "", true, true, true);

            gbc.gridx = 0; // 列索引为 0
            gbc.gridy = 3; // 行索引为 0
            gbc.anchor = GridBagConstraints.LINE_START; // 组件对齐方式为左对齐
            gbc.insets = JBUI.insets(5); // 设置组件之间的间距
            mainPanel.add(paramLabel, gbc); // 将标签添加到面板

            gbc.gridx = 1; // 列索引为 1
            gbc.gridy = 3; // 行索引为 0
            gbc.fill = GridBagConstraints.HORIZONTAL; // 水平拉伸组件
            gbc.weightx = 1.0; // 设置组件在水平方向上的拉伸权重
            mainPanel.add(paramTextField, gbc);


            JLabel returnLabel = new JLabel("Return:");
            returnTextField = new TextFieldWithCompletion(project, new ClassComplete(project, false), "", true, true, true);

            gbc.gridx = 0; // 列索引为 0
            gbc.gridy = 4; // 行索引为 0
            gbc.anchor = GridBagConstraints.LINE_START; // 组件对齐方式为左对齐
            gbc.insets = JBUI.insets(5); // 设置组件之间的间距
            mainPanel.add(returnLabel, gbc); // 将标签添加到面板

            gbc.gridx = 1; // 列索引为 1
            gbc.gridy = 4; // 行索引为 0
            gbc.fill = GridBagConstraints.HORIZONTAL; // 水平拉伸组件
            gbc.weightx = 1.0; // 设置组件在水平方向上的拉伸权重
            mainPanel.add(returnTextField, gbc);

            JLabel serviceLabel = new JLabel("Service Name:");
            serviceTextField = new TextFieldWithCompletion(project, new ClassComplete(project, false), "", true, true, true);
            gbc.gridx = 0; // 列索引为 0
            gbc.gridy = 5; // 行索引为 0
            gbc.anchor = GridBagConstraints.LINE_START; // 组件对齐方式为左对齐
            gbc.insets = JBUI.insets(5); // 设置组件之间的间距
            mainPanel.add(serviceLabel, gbc); // 将标签添加到面板

            gbc.gridx = 1; // 列索引为 1
            gbc.gridy = 5; // 行索引为 0
            gbc.fill = GridBagConstraints.HORIZONTAL; // 水平拉伸组件
            gbc.weightx = 1.0; // 设置组件在水平方向上的拉伸权重
            mainPanel.add(serviceTextField, gbc);

            gbc.gridx = 0; // 列索引为 0
            gbc.gridy = 6; // 行索引为 1
            gbc.fill = GridBagConstraints.HORIZONTAL; // 水平拉伸组件
            gbc.weightx = 1.0; // 设置组件在水平方向上的拉伸权重
            mainPanel.add(button, gbc);
        }

        private void buttonBindAction() {
            button = new JButton("Generate");
            button.addActionListener(e -> {
                // 在这里处理保存按钮的逻辑
                String text = textField.getText();
                validInput();
                Caret currentCaret = editor.getCaretModel().getCurrentCaret();
                int offset = currentCaret.getOffset();
                PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
                PsiFile psiFile = psiDocumentManager.getPsiFile(editor.getDocument());
                WriteCommandAction.runWriteCommandAction(editor.getProject(), () -> {
                    if(psiFile == null){
                        return;
                    }
                    if(!(psiFile instanceof PsiJavaFile)){
                        return;
                    }
                    // 格式化代码
                    CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
                    PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                    // 创建 PsiElementFactory
                    PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);

                    // 创建新的方法
                    StringBuilder sb = new StringBuilder();
                    String returnName = StringUtils.isBlank(returnTextField.getText()) ? "void" : returnTextField.getText().trim();
                    //TODO: add import list
                    String methodType = (String) methodTypes.getSelectedItem();
                    if(StringUtils.isBlank(methodType) || "Other".equals(methodType)){
                        methodType = "Request";
                    }
                    sb.append("@").append(methodType).append("Mapping(\"").append(urlTextField.getText()).append("\")\n");

                    if(!Objects.equals("Get", methodType) && !Objects.equals("Request", methodType)){
                        sb.append("@ResponseBody").append("\n");
                    }
                    sb.append("public ").append(returnName).append(" ").append(text).append("(");
                    String paramText = paramTextField.getText();
                    if(StringUtils.isNotBlank(paramText)){
                        String[] params = paramText.trim().split(",");
                        List<String> paramList = Arrays.stream(params).map(x -> x.trim() + " " + StringUtil.decapitalize(x.trim())).collect(Collectors.toList());
                        sb.append(StringUtils.join(paramList, ", "));
                        //TODO: add import list
                    }
                    sb.append(") {\n").append("    // TODO: Implement method");
                    if(!Objects.equals("void", returnName)){
                        if(StringUtils.isBlank(serviceTextField.getText())){
                            sb.append("\nreturn null;");
                        }else {
                            PsiField[] fields = psiJavaFile.getClasses()[0].getFields();
                            String fieldName = "";
                            for (PsiField field : fields) {
                                PsiType fieldType = field.getType();
                                String fieldTypeName = fieldType.getPresentableText();
                                if(Objects.equals(fieldTypeName, serviceTextField.getText())){
                                    //找到同一个属性了
                                    fieldName = field.getName();
                                    break;
                                }
                            }
                            if(StringUtils.isBlank(fieldName)){
                                fieldName = StringUtil.decapitalize(serviceTextField.getText());
                                StringBuilder fieldStr = new StringBuilder("@Resource\nprivate ");
                                fieldStr.append(serviceTextField.getText()).append(" ").append(fieldName).append(";");
                                PsiField fieldFromText = elementFactory.createFieldFromText(fieldStr.toString(), psiFile);
                                psiJavaFile.getClasses()[0].add(codeStyleManager.reformat(fieldFromText));
                                //TODO: add import list
                            }
                            sb.append("\nreturn ").append(fieldName).append(".").append(text).append("();");
                        }
                    }
                    sb.append("\n}");
                    PsiMethod newMethod = elementFactory.createMethodFromText(sb.toString(), psiFile);
                    PsiElement reformatMethod = codeStyleManager.reformat(newMethod);
                    if(psiFile.findElementAt(offset) == null){
                        psiJavaFile.getClasses()[0].add(reformatMethod);
                    }else {
                        psiJavaFile.getClasses()[0].addAfter(reformatMethod, psiFile.findElementAt(offset));
                    }

                    boolean isExit = false;
                    PsiImportList importList = psiJavaFile.getImportList();
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

                    codeStyleManager.reformat(psiFile);

                    // 移动光标到生成的方法的起始位置
                    editor.getCaretModel().moveToOffset(offset + 2); // +2 为插入的两个换行符
                });
                popup.cancel();
            });
        }

        private void validInput() {
            textField.setInputVerifier(new InputVerifier() {
                @Override
                public boolean verify(JComponent jComponent) {
                    String text = textField.getText();
                    if(StringUtils.isBlank(text)){
                        Messages.showMessageDialog("Method name required", "Error", Messages.getInformationIcon());
                        return false;
                    }
                    return true;
                }
            });
        }

        public JPanel getMainPanel() {
            return mainPanel;
        }
    }

    class ClassComplete extends TextFieldCompletionProvider {
        Project project;

        boolean many;

        public ClassComplete(Project project, boolean many){
            this.project = project;
            this.many = many;
        }

        @Override
        public @Nullable String getPrefix(@NotNull String text, int offset) {
            if(many){
                String substring = text.substring(0, offset);
                String[] split = substring.split(",");
                return split[split.length-1];
            }else {
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
                        .withBoldness(true).withTailText(packageName, true);
                result.addElement(lookupElementBuilder);
            }
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
            }
            return text;
        }
    }






}

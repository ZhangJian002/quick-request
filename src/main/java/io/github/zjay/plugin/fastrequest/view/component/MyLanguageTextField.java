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

package io.github.zjay.plugin.fastrequest.view.component;

import com.alibaba.fastjson.JSONObject;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.json.JsonFileType;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.xhtml.XHTMLLanguage;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.SoftWrapChangeListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.EditorTabbedContainer;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.ui.ErrorStripeEditorCustomization;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.concurrency.AppExecutorUtil;
import free.icons.PluginIcons;
import io.github.zjay.plugin.fastrequest.config.Constant;
import io.github.zjay.plugin.fastrequest.util.ToolWindowUtil;
import io.github.zjay.plugin.fastrequest.view.FastRequestToolWindow;
import org.cef.browser.CefBrowser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MyLanguageTextField extends LanguageTextField {

    private Project myProject;
    private FileType fileType;
    private Language language;

    public boolean isViewer;

    public boolean needPretty;

    JButton button;

    public MyLanguageTextField(Project myProject, Language language, FileType fileType, boolean isViewer, boolean needPretty) {
        super(language, myProject, "", false);
        this.myProject = myProject;
        this.fileType = fileType;
        this.language = language;
        this.isViewer = isViewer;
        this.needPretty = needPretty;
    }

    @Override
    protected @NotNull EditorEx createEditor() {
        EditorEx editor = super.createEditor();
        setUpEditor(editor);
        editor.putUserData(Constant.KEY_QUICKREQUEST, 1);
        editor.setViewer(isViewer);
        return editor;
    }

    @Override
    public void setText(@Nullable String text) {
        Language finalLanguage = getLanguage(text);
        ApplicationManager.getApplication().invokeAndWait(() -> {
            Document document = createDocument(text, finalLanguage, myProject, new SimpleDocumentCreator());
            setDocument(document);
            PsiFile psiFile = PsiDocumentManager.getInstance(myProject).getPsiFile(document);
            if (psiFile != null) {
                WriteCommandAction.runWriteCommandAction(
                        myProject,
                        () -> {
                            CodeStyleManager.getInstance(getProject()).reformat(psiFile);
                        }
                );
            }
        });
    }

    private Language getLanguage(String text) {
        if(!needPretty){
            return language;
        }
        Language myLanguage = null;
        try {
            JSONObject.parseObject(text);
            myLanguage = JsonLanguage.INSTANCE;
            super.setFileType(JsonFileType.INSTANCE);
        }catch (Exception e){
            //ignore
        }
        if(myLanguage == null){
            if(text.matches("<\\s*html[\\s\\S]*>")){
                myLanguage = HTMLLanguage.INSTANCE;
                super.setFileType(HtmlFileType.INSTANCE);
            }else if(text.matches("<\\s*[A-Za-z][A-Za-z0-9_]*[\\s\\S]*>")){
                myLanguage = XMLLanguage.INSTANCE;
                super.setFileType(XmlFileType.INSTANCE);
            }else {
                myLanguage = PlainTextLanguage.INSTANCE;
                super.setFileType(PlainTextFileType.INSTANCE);
            }
        }
        return myLanguage;
    }

    private void setUpEditor(EditorEx editor) {
        editor.offsetToVisualPosition(100);
        editor.offsetToLogicalPosition(100);
        editor.setHorizontalScrollbarVisible(true);
        editor.setShowPlaceholderWhenFocused(true);
        editor.setVerticalScrollbarVisible(true);
        editor.setCaretEnabled(true);
        editor.setEmbeddedIntoDialogWrapper(true);
        EditorSettings settings = editor.getSettings();
        settings.setLeadingWhitespaceShown(true);
        settings.setTrailingWhitespaceShown(true);
        settings.setGutterIconsShown(true);
        settings.setSmartHome(true);
        settings.setLineNumbersShown(true);
        settings.setIndentGuidesShown(true);
        settings.setUseSoftWraps(true);
        settings.setAdditionalLinesCount(3);
        settings.setAutoCodeFoldingEnabled(true);
        settings.setFoldingOutlineShown(true);
        settings.setAllowSingleLogicalLineFolding(true);
        settings.setRightMarginShown(true);
        settings.setCaretRowShown(true);
        settings.setLineMarkerAreaShown(true);
        settings.setDndEnabled(true);

        //开启右侧的错误条纹
        ErrorStripeEditorCustomization.ENABLED.customize(editor);
    }

    public void setMyProject(Project myProject) {
        this.myProject = myProject;
    }

    @Override
    public void setFileType(@NotNull FileType fileType) {
        this.fileType = fileType;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }



}

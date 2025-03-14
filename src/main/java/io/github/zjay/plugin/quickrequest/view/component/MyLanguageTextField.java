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

package io.github.zjay.plugin.quickrequest.view.component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.json.JsonFileType;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.ui.ErrorStripeEditorCustomization;
import com.intellij.ui.LanguageTextField;
import com.intellij.util.LocalTimeCounter;
import io.github.zjay.plugin.quickrequest.config.Constant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class MyLanguageTextField extends LanguageTextField {


    private Project myProject;
    private FileType fileType;
    private Language language;

    public boolean isViewer;

    public boolean needPretty;

    private int type;

    private EditorEx editor;

    public MyLanguageTextField(Project myProject, Language language, FileType fileType, boolean isViewer, boolean needPretty, int type) {
        super(language, myProject, "", false);
        this.myProject = myProject;
        this.fileType = fileType;
        this.language = language;
        this.isViewer = isViewer;
        this.needPretty = needPretty;
        this.type = type;
    }

    @Override
    protected @NotNull EditorEx createEditor() {
        EditorEx editor = super.createEditor();
        setUpEditor(editor);
        editor.putUserData(Constant.KEY_QUICKREQUEST, type);
        editor.setViewer(isViewer);
        this.editor = editor;
        return editor;
    }

    @Override
    public void setText(@Nullable String text) {
        if(text == null){
            text = "";
        }
        String finalText = text;
        Language finalLanguage = getLanguage(finalText);
        LanguageFileType associatedFileType = finalLanguage.getAssociatedFileType();
        updateFileLanguage(associatedFileType, text);
    }

    public void updateFileLanguage(FileType associatedFileType, String finalText){
        PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(myProject);
        ApplicationManager.getApplication().invokeAndWait(() -> {
            PsiFile psiFile = psiFileFactory.createFileFromText("ZJay." + associatedFileType.getDefaultExtension(), associatedFileType, finalText, LocalTimeCounter.currentTime(), true, false);
            SimpleDocumentCreator simpleDocumentCreator = new SimpleDocumentCreator();
            simpleDocumentCreator.customizePsiFile(psiFile);
            Document document = PsiDocumentManager.getInstance(myProject).getDocument(psiFile);
            setDocument(document);
            PsiDocumentManager.getInstance(myProject).commitDocument(document);
            WriteCommandAction.runWriteCommandAction(
                    myProject,
                    () -> {
                        CodeStyleManager.getInstance(getProject()).reformat(psiFile);
                        if (editor != null) {
                            editor.getScrollingModel().scroll(0, 0);
                        }
                    }
            );
        });
    }

    private Language getLanguage(String text) {
        if (!needPretty) {
            return PlainTextLanguage.INSTANCE;
        }
        Language myLanguage = null;
        try {
            if(JSONValidator.from(text).validate()){
                myLanguage = JsonLanguage.INSTANCE;
                super.setFileType(JsonFileType.INSTANCE);
            }
        }catch (Exception e){

        }
        if (myLanguage == null) {
            if (text.matches("<\\s*html[\\s\\S]*>")) {
                myLanguage = HTMLLanguage.INSTANCE;
                super.setFileType(HtmlFileType.INSTANCE);
            } else if (text.matches("<\\s*[A-Za-z][A-Za-z0-9_]*[\\s\\S]*>")) {
                myLanguage = XMLLanguage.INSTANCE;
                super.setFileType(XmlFileType.INSTANCE);
            } else {
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

    @Override
    public FileType getFileType() {
        return fileType;
    }

    public Language getLanguage() {
        return language;
    }
}

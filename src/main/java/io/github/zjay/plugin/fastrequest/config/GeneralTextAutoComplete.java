package io.github.zjay.plugin.fastrequest.config;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.TextFieldCompletionProvider;
import com.intellij.xml.util.HtmlUtil;
import com.intellij.xml.util.documentation.MimeTypeDictionary;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeneralTextAutoComplete extends TextFieldCompletionProvider {

    private final Constant.AutoCompleteType autoCompleteType;

    private List<String> items;

    public GeneralTextAutoComplete(Constant.AutoCompleteType autoCompleteType){
        this.autoCompleteType = autoCompleteType;
        addItem();
    }

    public void addItem(){
        switch (autoCompleteType){
            case Header_Name:
                items = new ArrayList<>(Arrays.asList(HtmlUtil.RFC2616_HEADERS));
                items.add("token");
                items.add("Token");
                break;
            case Header_value:
                items = Arrays.asList(MimeTypeDictionary.HTML_CONTENT_TYPES);
                break;
            default:
                items = new ArrayList<>();
        }
    }

    @Override
    protected void addCompletionVariants(@NotNull String text, int offset, @NotNull String prefix, @NotNull CompletionResultSet result) {
        for (String target : items) {
            LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(target);
            result.addElement(lookupElementBuilder);
        }
    }
}

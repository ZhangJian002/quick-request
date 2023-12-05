package io.github.zjay.plugin.fastrequest.view.linemarker.tooltip;

import com.intellij.psi.PsiElement;
import io.github.zjay.plugin.fastrequest.util.LanguageEnum;

public class BaseFunctionTooltip {

    String msg = "Generate Request For ";
    private final PsiElement element;

    private final LanguageEnum language;

    public BaseFunctionTooltip(PsiElement element, LanguageEnum language) {
        this.element = element;
        this.language = language;
    }

    public PsiElement getElement() {
        return element;
    }

    public LanguageEnum getLanguage() {
        return language;
    }
}

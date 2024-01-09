package io.github.zjay.plugin.quickrequest.ui;


import com.intellij.psi.PsiElement;
import io.github.zjay.plugin.quickrequest.model.ApiService;

@FunctionalInterface
public interface PhpFirstFunction {

    ApiService doIt(PsiElement psiElement);
}

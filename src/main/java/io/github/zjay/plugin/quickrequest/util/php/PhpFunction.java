package io.github.zjay.plugin.quickrequest.util.php;


import com.intellij.psi.PsiElement;
import io.github.zjay.plugin.quickrequest.model.ApiService;


@FunctionalInterface
public interface PhpFunction {

    void doIt(PsiElement psiElement, ApiService apiService);
}

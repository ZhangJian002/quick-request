// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package io.github.zjay.plugin.fastrequest.configurable;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.NavigateAction;
import com.intellij.diagnostic.PluginException;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.MarkupEditorFilter;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.SeparatorPlacement;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.Function;
import io.github.zjay.plugin.fastrequest.action.LineMarkerRightClickAction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

public class MyLineMarkerInfo<T extends PsiElement> extends LineMarkerInfo<PsiElement> {

  public MyLineMarkerInfo(@NotNull PsiElement element, @NotNull TextRange range, @NotNull Icon icon, @Nullable Function<? super PsiElement, @NlsContexts.Tooltip String> tooltipProvider, @Nullable GutterIconNavigationHandler<PsiElement> navHandler, GutterIconRenderer.@NotNull Alignment alignment, @NotNull Supplier<@NotNull @Nls String> accessibleNameProvider) {
    super(element, range, icon, tooltipProvider, navHandler, alignment, accessibleNameProvider);
  }

  @Override
  public GutterIconRenderer createGutterRenderer() {
    return myIcon == null ? null : new MyLineMarkerGutterIconRenderer<>(this);
  }

  public static class MyLineMarkerGutterIconRenderer<T extends PsiElement> extends LineMarkerInfo.LineMarkerGutterIconRenderer<PsiElement> {
    private final MyLineMarkerInfo<T> myInfo;

    @Override
    public @Nullable AnAction getRightButtonClickAction() {
      return new LineMarkerRightClickAction(myInfo, this);
    }

    public MyLineMarkerGutterIconRenderer(@NotNull MyLineMarkerInfo<T> info) {
      super(info);
      myInfo = info;
    }

  }
}

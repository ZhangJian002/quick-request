// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package io.github.zjay.plugin.quickrequest.config.configurable;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import io.github.zjay.plugin.quickrequest.config.action.LineMarkerRightClickAction;
import io.github.zjay.plugin.quickrequest.config.linemarker.tooltip.BaseFunctionTooltip;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.function.Supplier;

public class MyLineMarkerInfo<T extends PsiElement> extends LineMarkerInfo<PsiElement> {

  private BaseFunctionTooltip functionTooltip;

  public MyLineMarkerInfo(@NotNull PsiElement element, @NotNull TextRange range, @NotNull Icon icon, @Nullable Function<? super PsiElement, @NlsContexts.Tooltip String> tooltipProvider, @Nullable GutterIconNavigationHandler<PsiElement> navHandler, GutterIconRenderer.@NotNull Alignment alignment, @NotNull Supplier<@NotNull String> accessibleNameProvider) {
    super(element, range, icon, tooltipProvider, navHandler, alignment, accessibleNameProvider);
    this.functionTooltip = (BaseFunctionTooltip) tooltipProvider;
  }

  public BaseFunctionTooltip getFunctionTooltip(){
    return functionTooltip;
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

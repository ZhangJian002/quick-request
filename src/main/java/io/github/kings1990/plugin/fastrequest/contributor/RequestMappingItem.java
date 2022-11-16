/*
 * Copyright 2021 kings1990(darkings1990@gmail.com)
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

package io.github.kings1990.plugin.fastrequest.contributor;

import com.intellij.find.impl.FindInProjectUtil;
import com.intellij.icons.AllIcons;
import com.intellij.ide.structureView.impl.java.ClassInitializerTreeElement;
import com.intellij.model.presentation.SymbolDeclarationPresentation;
import com.intellij.navigation.ColoredItemPresentation;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.SyntaxHighlighterColors;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import io.github.kings1990.plugin.fastrequest.util.FrIconUtil;
import org.intellij.plugins.markdown.structureView.MarkdownStructureElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;


public  class RequestMappingItem implements NavigationItem {
   private final Navigatable navigationElement;
   @NotNull
   private final PsiElement psiElement;
   private final String urlPath;
   private final String requestMethod;

   @Override
   @NotNull
   public String getName() {
      return this.requestMethod + " " +this.urlPath;
   }

   @Override
   @NotNull
   public ItemPresentation getPresentation() {
      return new RequestMappingItem.RequestMappingItemPresentation();
   }

   @Override
   public void navigate(boolean requestFocus) {
      if (navigationElement != null) {
         navigationElement.navigate(requestFocus);
      }
   }



   @Override
   public boolean canNavigate() {
      return this.navigationElement != null;
   }

   @Override
   public boolean canNavigateToSource() {
      return true;
   }

   @Override
   @NotNull
   public String toString() {
      return "RequestMappingItem(psiElement=" + this.psiElement + ", urlPath='" + this.urlPath + "', requestMethod='" + this.requestMethod + "', navigationElement=" + this.navigationElement + ')';
   }

   @NotNull
   public final PsiElement getPsiElement() {
      return this.psiElement;
   }

   public RequestMappingItem(@NotNull PsiElement psiElement, @NotNull String urlPath, @NotNull String requestMethod) {
      this.psiElement = psiElement;
      this.urlPath = urlPath;
      this.requestMethod = requestMethod;
      PsiElement var10001 = this.psiElement.getNavigationElement();
      if (!(var10001 instanceof Navigatable)) {
         var10001 = null;
      }

      this.navigationElement = (Navigatable)var10001;
   }

   public final class RequestMappingItemPresentation implements ColoredItemPresentation {
      @Override
      @NotNull
      public String getPresentableText() {
         return RequestMappingItem.this.urlPath;
      }

      @Override
      @NotNull
      public String getLocationString() {
         PsiElement psiElement = RequestMappingItem.this.getPsiElement();
         PsiFile psiFile = psiElement.getContainingFile();
         String fileName = psiFile != null ? psiFile.getName() : null;
         if (psiElement instanceof PsiMethod) {
            return fileName != null ? fileName : "unknownLocation" +  "." + ((PsiMethod) psiElement).getName();
         } else if (psiElement instanceof PsiClass){
            return fileName != null ? fileName : "unknownLocation";
         } else {
            return "unknownLocation";
         }
      }

      @Override
      @NotNull
      public Icon getIcon(boolean b) {
         PsiElement psiElement = RequestMappingItem.this.getPsiElement();
         PsiFile psiFile = psiElement.getContainingFile();
         PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
         return FrIconUtil.getIconByMethodAndClassType(requestMethod, psiJavaFile.getClasses()[0].isInterface());
      }

      @Override
      public @Nullable TextAttributesKey getTextAttributesKey() {
         return null;
//         return EditorColors.SEARCH_RESULT_ATTRIBUTES;
      }
   }
}

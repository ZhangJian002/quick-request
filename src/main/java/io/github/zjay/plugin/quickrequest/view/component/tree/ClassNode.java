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

package io.github.zjay.plugin.quickrequest.view.component.tree;

import com.goide.GoIcons;
import com.intellij.icons.AllIcons;
import icons.PhpIcons;
import icons.PythonIcons;
import io.github.zjay.plugin.quickrequest.util.LanguageEnum;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.KotlinIcons;

import javax.swing.*;

public class ClassNode extends BaseNode<String> {

    LanguageEnum languageEnum;

    public ClassNode(String className, LanguageEnum languageEnum) {
        super(className);
        this.languageEnum = languageEnum;
    }

    @Override
    public @Nullable Icon getIcon(boolean selected) {
        switch (languageEnum){
            case go:
                return GoIcons.ICON;
            case php:
                return PhpIcons.PhpIcon;
            case Python:
                return PythonIcons.Python.Python;
            case Kotlin:
                return KotlinIcons.CLASS;
        }
        //默认返回class
        return AllIcons.Nodes.Class;
    }
}

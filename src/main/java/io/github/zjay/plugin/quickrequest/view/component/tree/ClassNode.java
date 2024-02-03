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
import icons.RubyIcons;
import io.github.zjay.plugin.quickrequest.util.LanguageEnum;
import io.github.zjay.plugin.quickrequest.util.ReflectUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.KotlinIcons;
import quickRequest.icons.PluginIcons;

import javax.swing.*;
import java.lang.reflect.Constructor;

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
                return PluginIcons.ICON_PYTHON;
            case Kotlin:
                return KotlinIcons.CLASS;
            case Ruby:
                return RubyIcons.Ruby.Ruby;
            case Rust:
                Object instance = ReflectUtils.getInstance("org.rust.ide.icons.RsIcons");
                Object rustIcon = ReflectUtils.invokeMethod(instance, "getRUST");
                if(rustIcon instanceof Icon){
                    return (Icon)rustIcon;
                }
        }
        //默认返回class
        return AllIcons.Nodes.Class;
    }
}

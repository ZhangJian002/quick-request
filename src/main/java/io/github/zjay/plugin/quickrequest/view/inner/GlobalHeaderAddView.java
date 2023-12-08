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

package io.github.zjay.plugin.quickrequest.view.inner;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.textCompletion.TextFieldWithCompletion;
import io.github.zjay.plugin.quickrequest.config.Constant;
import io.github.zjay.plugin.quickrequest.complete.GeneralTextAutoComplete;
import io.github.zjay.plugin.quickrequest.deprecated.MyComponentPanelBuilder;
import io.github.zjay.plugin.quickrequest.deprecated.MyPanelGridBuilder;
import io.github.zjay.plugin.quickrequest.model.DataMapping;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class GlobalHeaderAddView extends DialogWrapper {
    private TextFieldWithCompletion keyTextField;
    private TextFieldWithCompletion valueTextField;

    private Project project;

    public GlobalHeaderAddView(Project project) {
        super(false);
        this.project = project;
        init();
        setSize(500, 100);
        setTitle("Add Global Request Headers");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        Project defaultProject = ProjectManager.getInstance().getDefaultProject();
        keyTextField = new TextFieldWithCompletion(defaultProject, new GeneralTextAutoComplete(Constant.AutoCompleteType.Header_Name), "", true, true, false);;
        valueTextField = new TextFieldWithCompletion(defaultProject, new GeneralTextAutoComplete(Constant.AutoCompleteType.Header_value), "", true, true, false);;
        return new MyPanelGridBuilder().splitColumns()
                .add(new MyComponentPanelBuilder(keyTextField).withLabel("Key"))
                .add(new MyComponentPanelBuilder(valueTextField).withLabel("Value"))
                .createPanel();
    }

    protected ValidationInfo doValidate() {
        if (StringUtils.isEmpty(keyTextField.getText())) {
            return new ValidationInfo("Please set key");
        }
        if (StringUtils.isEmpty(valueTextField.getText())) {
            return new ValidationInfo("Please set value");
        }
        return super.doValidate();
    }

    public DataMapping getValue() {
        return new DataMapping(keyTextField.getText(), valueTextField.getText());
    }
}

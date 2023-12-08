package io.github.zjay.plugin.quickrequest.deprecated;


import com.intellij.openapi.actionSystem.AnAction;

@FunctionalInterface
public interface AddAnActionFunction {

    AnAction initAnAction();
}

package io.github.zjay.plugin.quickrequest.config;


import com.intellij.openapi.actionSystem.AnAction;

@FunctionalInterface
public interface AddAnActionFunction {

    AnAction initAnAction();
}

package io.github.zjay.plugin.fastrequest.config;


import com.intellij.openapi.actionSystem.AnAction;

@FunctionalInterface
public interface AddAnActionFunction {

    AnAction initAnAction();
}

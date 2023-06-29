package io.github.zjay.plugin.fastrequest.config;


import cn.hutool.http.HttpRequest;

@FunctionalInterface
public interface PressureFunction {

    HttpRequest buildRequest();
}

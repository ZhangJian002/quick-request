package io.github.zjay.plugin.fastrequest.jmh;


import cn.hutool.http.HttpRequest;

@FunctionalInterface
public interface BuildRequestFunction {

    HttpRequest buildRequest();
}

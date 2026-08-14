package io.github.zjay.plugin.quickrequest.util;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

/**
 * QuickRequest 插件的国际化 bundle。
 * <p>
 * 资源文件位于 src/main/resources/messages/QuickRequestBundle.properties（默认英文）
 * 及 QuickRequestBundle_zh_CN.properties（中文覆盖）。
 */
public final class QuickRequestBundle extends DynamicBundle {

    @NonNls
    public static final String BUNDLE = "messages.QuickRequestBundle";

    private static final QuickRequestBundle INSTANCE = new QuickRequestBundle();

    private QuickRequestBundle() {
        super(BUNDLE);
    }

    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return INSTANCE.getMessage(key, params);
    }
}

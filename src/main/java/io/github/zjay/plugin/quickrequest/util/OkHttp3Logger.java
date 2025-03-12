package io.github.zjay.plugin.quickrequest.util;

import com.intellij.idea.IdeaLogger;
import com.intellij.openapi.diagnostic.Logger;
import io.github.zjay.plugin.quickrequest.config.FastRequestComponent;
import io.github.zjay.plugin.quickrequest.model.FastRequestConfiguration;
import okhttp3.logging.HttpLoggingInterceptor;

public class OkHttp3Logger implements HttpLoggingInterceptor.Logger{
    private final Logger LOGGER = IdeaLogger.getInstance(OkHttp3Util.class);
    private StringBuilder mMessage = new StringBuilder();

    @Override
    public void log(String message) {
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;
        if (config.getNeedIdeaLog() == null || !config.getNeedIdeaLog()){
            return;
        }
        // 请求或者响应开始
        if (message.startsWith("--> ") && !message.startsWith("--> END")) {
            mMessage.append("\n");
        }
        mMessage.append(message.concat("\n"));
        // 请求或者响应结束，打印整条日志
        if (message.startsWith("<-- END HTTP")) {
            LOGGER.info(mMessage.toString());
        }
    }
}

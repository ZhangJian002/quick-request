package io.github.zjay.plugin.quickrequest.util;

import io.github.zjay.plugin.quickrequest.config.FastRequestComponent;
import io.github.zjay.plugin.quickrequest.model.FastRequestConfiguration;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public class OkHttp3Util {

    //MEDIA_TYPE <==> Content-Type
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    //MEDIA_TYPE_TEXT post请求不是application/x-www-form-urlencoded的，全部直接返回，不作处理，即不会解析表单数据来放到request parameter map中。所以通过request.getParameter(name)是获取不到的。只能使用最原始的方式，读取输入流来获取。
    private static final MediaType MEDIA_TYPE_TEXT = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    public static OkHttpClient getSingleClientInstance() {
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;
        return new OkHttpClient.Builder()
                .connectTimeout(config.getConnectionTimeout() == null ? 60 : config.getConnectionTimeout(), TimeUnit.SECONDS)
                .readTimeout(config.getReadTimeout() == null ? 60 : config.getReadTimeout(), TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(1,10, TimeUnit.MILLISECONDS))
                .retryOnConnectionFailure(true)
                .build();
    }
}

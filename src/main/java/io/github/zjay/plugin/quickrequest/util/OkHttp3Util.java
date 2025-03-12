package io.github.zjay.plugin.quickrequest.util;

import io.github.zjay.plugin.quickrequest.config.FastRequestComponent;
import io.github.zjay.plugin.quickrequest.model.FastRequestConfiguration;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import java.util.concurrent.TimeUnit;

public class OkHttp3Util {
    public static  ConnectionPool connectionPool = new ConnectionPool(Runtime.getRuntime().availableProcessors()*2 ,10, TimeUnit.SECONDS);
    public static OkHttpClient getSingleClientInstance() {
        FastRequestConfiguration config = FastRequestComponent.getInstance().getState();
        assert config != null;
        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(new OkHttp3Logger());
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .connectTimeout(config.getConnectionTimeout() == null ? 60 : config.getConnectionTimeout(), TimeUnit.SECONDS)
                .readTimeout(config.getReadTimeout() == null ? 60 : config.getReadTimeout(), TimeUnit.SECONDS)
                .connectionPool(connectionPool)
                .retryOnConnectionFailure(false)
                //.addNetworkInterceptor(logInterceptor) // (encoded body omitted)
                .addInterceptor(logInterceptor) // (51-byte body)
                .build();
    }
}

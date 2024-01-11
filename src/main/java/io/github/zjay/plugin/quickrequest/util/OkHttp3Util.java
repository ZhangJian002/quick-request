package io.github.zjay.plugin.quickrequest.util;

import io.github.zjay.plugin.quickrequest.config.FastRequestComponent;
import io.github.zjay.plugin.quickrequest.model.FastRequestConfiguration;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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

    /**
     * @param url getUrl
     * @return java.lang.String
     * @author xiaobu
     * @date 2019/3/4 11:20
     * @descprition
     * @version 1.0
     */
    public static String sendByGetUrl(String url) {
        String result;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            assert response.body() != null;
            result = response.body().string();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param url , json
     * @return java.lang.String
     * @author xiaobu
     * @date 2019/3/4 11:13
     * @descprition
     * @version 1.0 post+json方式
     */
    public static String sendByPostJson(String url, String json) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            assert response.body() != null;
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    /**
     * @author xiaobu
     * @date 2019/3/4 15:58
     * @param url , params]
     * @return java.lang.String
     * @descprition  post方式请求
     * @version 1.0
     */
    public static String sendByPostMap(String url, Map<String, String> params) {
        String result;
        OkHttpClient client = new OkHttpClient();
        StringBuilder content = new StringBuilder();
        Set<Map.Entry<String, String>> entrys = params.entrySet();
        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            content.append(entry.getKey()).append("=").append(entry.getValue());
            if (iterator.hasNext()) {
                content.append("&");
            }
        }

        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_TEXT, content.toString());
        Request request = new Request.Builder().url(url).post(requestBody).build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            assert response.body() != null;
            result = response.body().string();
            System.out.println("result = " + result);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    public static void main(String[] args) {
        //String url = "http://172.18.9.166:8080/user/login.kq";
        //String url = "http://172.18.9.166:8899/demo/postByMap?loginName=admin&loginPassword=Hans1107laser";
        String url = "http://172.18.9.166:8899/demo/postByMap";
        Map<String, String> params = new HashMap<>();
        params.put("loginName", "admin");
        params.put("loginPassword", "123456");
        String result = sendByPostMap(url, params);
        System.out.println("result = " + result);

    }

}

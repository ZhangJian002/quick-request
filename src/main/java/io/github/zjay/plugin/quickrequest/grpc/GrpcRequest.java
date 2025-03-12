package io.github.zjay.plugin.quickrequest.grpc;

import java.util.List;
import java.util.Map;

public class GrpcRequest {

    private String grpcurlPath;

    private boolean isTls;

    private String protoPath;

    private String protoFile;

    private String data;

    private String host;

    private int port;

    private String service;

    private String method;

    private Map<String, String> headerMap;

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }

    public void setHeaderMap(Map<String, String> headerMap) {
        this.headerMap = headerMap;
    }

    public String getGrpcurlPath() {
        return grpcurlPath;
    }

    public void setGrpcurlPath(String grpcurlPath) {
        this.grpcurlPath = grpcurlPath;
    }

    public boolean isTls() {
        return isTls;
    }

    public void setTls(boolean tls) {
        isTls = tls;
    }

    public String getProtoPath() {
        return protoPath;
    }

    public void setProtoPath(String protoPath) {
        this.protoPath = protoPath;
    }

    public String getProtoFile() {
        return protoFile;
    }

    public void setProtoFile(String protoFile) {
        this.protoFile = protoFile;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}

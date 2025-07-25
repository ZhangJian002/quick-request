package io.github.zjay.plugin.quickrequest.grpc;

import com.intellij.openapi.util.SystemInfo;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

public class GrpcCurlUtils {

    public static final String GRPC_PATH_DEFAULT = "grpcurl";

    static String requestCmd = "$GrpcurlPath $TLS $headers -import-path $ImportPath --proto $ProtoFile $Data $host:$port $Service/$Method";

    public static boolean existGrpcCurl(String grpcurlPath) {
        Process process = null;
        try {
            if (StringUtils.isBlank(grpcurlPath)){
                grpcurlPath = GRPC_PATH_DEFAULT;
            }
            grpcurlPath += " -version";
            process = getProcess(grpcurlPath);
            int code = process.waitFor();
            return code == 0;
        }catch (Exception e) {
            //ignore
//            throw new RuntimeException("grpcurl cannot be executed! " + e.getMessage() , e);
        }finally {
            if(process != null){
                process.destroy();
            }
        }
        return false;
    }

    public static String[] request(GrpcRequest grpcRequest) {
        Process process = null;
        try{
            String finalCmd = getRealCmd(grpcRequest);
            process = getProcess(finalCmd);
            int exitCode = process.waitFor();
            String result = readAndGetResult(process, exitCode == 0);
            return new String[]{result, finalCmd, exitCode+""};
        } catch (Exception e) {
            throw new RuntimeException("grpcurl cannot be executed!", e);
        }finally {
            if(process != null){
                process.destroy();
            }
        }
    }

    private static String readAndGetResult(Process process, boolean success) {
        BufferedReader reader;
        StringBuilder output = new StringBuilder();
        try {
            InputStream is;
            if (success){
                is = process.getInputStream();
            }else {
                is = process.getErrorStream();
            }
            reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return output.toString();
    }

    private static @NotNull String getRealCmd(GrpcRequest grpcRequest) {
        String tls = "";
        if (!grpcRequest.isTls()){
            tls = "-plaintext";
        }
        StringBuilder headers = new StringBuilder();
        if (grpcRequest.getHeaderMap() != null && !grpcRequest.getHeaderMap().isEmpty()){
            for (String key : grpcRequest.getHeaderMap().keySet()) {
                headers.append("-H  \"").append(key).append(":").append(grpcRequest.getHeaderMap().get(key)).append("\" ");
            }
        }
        String result = requestCmd.replace("$GrpcurlPath", grpcRequest.getGrpcurlPath()).replace("$TLS", tls)
                .replace("$headers", headers).replace("$ImportPath", grpcRequest.getProtoPath())
                .replace("$ProtoFile", grpcRequest.getProtoFile())
                .replace("$host", grpcRequest.getHost()).replace("$port", grpcRequest.getPort() + "")
                .replace("$Service", grpcRequest.getService()).replace("$Method", grpcRequest.getMethod());
        if (StringUtils.isNotBlank(grpcRequest.getData())){
            result = result.replace("$Data", "-d " + grpcRequest.getData());
        }else {
            result = result.replace("$Data", "");
        }
        return result;
    }

    private static Process getProcess(String cmd) throws IOException {
        Process process;
        StringTokenizer st = new StringTokenizer(cmd);
        if (SystemInfo.isWindows){
            String[] cmdarray = new String[st.countTokens()];
            for (int i = 0; st.hasMoreTokens(); i++)
                cmdarray[i] = st.nextToken();
            process = new ProcessBuilder(cmdarray).start();
        } else {
            String[] cmdarray = new String[3];
            cmdarray[0] = "/bin/bash";
            cmdarray[1] = "-c";
            cmdarray[2] = cmd;
            process = new ProcessBuilder(cmdarray).start();
        }
        return process;
    }
}

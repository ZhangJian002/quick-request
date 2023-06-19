package io.github.zjay.plugin.fastrequest.dubbo;


import java.io.*;
import java.net.Socket;

public class InvokeService {
    public String[] invoke(DubboService dubboService) {
        String[] addressSplit = dubboService.getServiceAddress().split(":");
        String ip = addressSplit[0];
        int port = Integer.valueOf(addressSplit[1]);
        Socket socket = null;
        try {
            socket = new Socket(ip, port);
            InputStream in = socket.getInputStream();;
            PrintStream out = new PrintStream(socket.getOutputStream(), true, "UTF-8");
            String command = String.format("invoke %s(%s)", dubboService.getServiceName(), dubboService.getParam().getDubboParamString());
            out.println(command);
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {

            }
            StringBuilder sb = new StringBuilder();
            BufferedInputStream bi = new BufferedInputStream(in);
            while (true) {
                byte[] buffer = new byte[1024];
                int len = bi.read(buffer);
                if (len <= -1) {
                    break;
                }
                String msg = new String(buffer, "UTF-8");
                sb.append(msg.trim());
                if (msg.endsWith("dubbo>") || sb.indexOf("dubbo>") != -1) {
                    break;
                }
            }
            String result = sb.toString().replace("\r\ndubbo>", "");
            String[] split = result.split("\r\n");
            for (String s : split) {
                if(s.contains("result")){
                    return new String[]{result, s.replace("result: ", "")};
                }
            }
            return new String[]{result, result};
        } catch (IOException e) {
            return new String[]{e.getMessage(), e.getMessage()};
        }finally {
            if(socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }

}

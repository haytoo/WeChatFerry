package com.hongwuyun;

import io.sisu.nng.NngException;
import io.sisu.nng.pair.Pair1Socket;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Objects;

@Getter
@ToString
@Slf4j
public class Wcferry {

    /**
     * 微信运行在远程机器
     */
    private final Boolean wechatIsRemote;
    @Setter
    /**
     * 调试模式
     */
    private Boolean isDebug;

    /**
     * 机器人IP
     */
    private final String host;
    /**
     * 机器人端口
     */
    private final Integer port;

    private String wcfExePath;

    /**
     *
     */
    public Wcferry() {
        this(Constant.DEFAULT_HOST, Constant.DEFAULT_PORT, false);
    }


    /**
     * @param port
     */
    public Wcferry(Integer port) {
        this(Constant.DEFAULT_HOST, port, false);
    }


    /**
     * @param host
     * @param port
     * @param wechatIsRemote
     */
    public Wcferry(String host, Integer port, Boolean wechatIsRemote) {
        this.host = host;
        this.port = port;
        this.wechatIsRemote = wechatIsRemote;
    }

    private void setDebug(Boolean isDebug) {
        this.isDebug = isDebug;
    }

    public void up() {
        URL resource = Wcferry.class.getResource(Constant.DEFAULT_EXE_PATH);
        if (Objects.isNull(resource)) {
            log.error("wcf.exe未找到");
            System.exit(1);
        }
        wcfExePath = resource.getFile();
        log.info("wcf.exe位于 {}", wcfExePath);

        String[] cmd = {wcfExePath, "start", Integer.toString(port), isDebug ? "debug" : ""};
//        log.info("启动命令{}",cmd);
        int status;
        try {
            status = Runtime.getRuntime().exec(cmd).waitFor();
        } catch (InterruptedException | IOException e) {
            log.error("机器人启动失败", e);
            throw new RuntimeException(e);
        }
        if (0 != status) {
            log.error("机器人启动失败:{}", status);
            throw new RuntimeException("机器人启动失败:" + status);
        } else {
            log.info("机器人启动成功:{}", status);
        }

        try {
            @Cleanup Pair1Socket socket = new Pair1Socket();
            String rpcUrl = MessageFormat.format("tcp://{0}:{1,number,#}", host, port);
            log.info("rpcUrl={}", rpcUrl);
            socket.dial(rpcUrl);
            log.info("rpc连接:{}", socket);
        } catch (NngException e) {
            log.error("连接机器人失败:", e);
            throw new RuntimeException(e);
        }

        registerShutdownHook();
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("关闭...");
            unHookMsg();
            if (!wechatIsRemote) {
                String[] cmd = new String[2];
                cmd[0] = wcfExePath;
                cmd[1] = "stop";
                int status;
                try {
                    status = Runtime.getRuntime().exec(cmd).waitFor();
                } catch (InterruptedException | IOException e) {
                    log.error("停止机器人失败", e);
                    throw new RuntimeException(e);
                }
                if (0 != status) {
                    log.error("停止机器人失败:{}", status);
                    throw new RuntimeException("停止机器人失败: " + status);
                }
            }
        }));
    }

    /**
     * 设置消息hook
     *
     * @return
     */
    public Boolean hookMsg() {
        return true;
    }

    /**
     * 取消消息hook
     *
     * @return
     */
    public Boolean unHookMsg() {
        log.info("取消消息hook");
        return true;
    }
}

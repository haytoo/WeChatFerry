package com.hongwuyun;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {
    public static void main(String[] args) throws InterruptedException {
        Wcferry client = new Wcferry();
        client.setIsDebug(true);
        client.up();

        log.info("机器人:{}", client);
        Thread.currentThread().join();
        log.info("来了吗");
    }
}

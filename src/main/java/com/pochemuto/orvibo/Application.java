package com.pochemuto.orvibo;

import com.pochemuto.orvibo.http.HttpServer;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 13.11.2016
 */
public class Application {

    public static void main(String... args) {
        Orvibo orvibo = new Orvibo();
        HttpServer server = new HttpServer(orvibo);
        server.init();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                orvibo.shutdown();
                server.shutdown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }

}

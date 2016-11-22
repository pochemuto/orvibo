package com.pochemuto.orvibo;

import com.pochemuto.orvibo.http.HttpServer;

/**
 * Entry point for http-based app
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 13.11.2016
 */
public class HttpGate {

    public static void main(String... args) {
        int port = HttpServer.DEFAULT_PORT;
        if (args.length > 1) {
            port = Integer.parseInt(args[0]);
        }

        Orvibo orvibo = new Orvibo();
        HttpServer server = new HttpServer(orvibo, port);
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

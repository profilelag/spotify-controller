package com.tiji.media;

import com.sun.net.httpserver.*;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class WebGuideServer {
    public static HttpServer server;

    static {
        try {
            server = HttpServer.create(new InetSocketAddress(25566), 0);
        } catch (IOException e) {
            Media.LOGGER.error("Unexpected error: {}", e);
        }
    }

    public static void start() throws IOException {
        server.createContext("/callback", new callbackHandler());
        server.createContext("/config", new configHandler());
        server.createContext("/", new rootHandler());

        server.setExecutor(null);
        server.start();
    }
    public static void stop() {
        server.stop(0);
    }
    private static class rootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response;
            int length;
            InputStream in;
            try{
                in = switch (MinecraftClient.getInstance().getLanguageManager().getLanguage()) {
                    case "ko_kr" -> getClass().getResourceAsStream("/guide/ko_kr.html");
                    default -> getClass().getResourceAsStream("/guide/en_us.html");
                };
                byte[] file = in.readAllBytes();
                length = file.length;
                response = new String(file);
            }catch(Exception e){
                e.printStackTrace();
                return;
            }
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    private static class callbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String Code = exchange.getRequestURI().getQuery().split("=")[1];
            MediaClient.CONFIG.accessToken(Code);

            Media.LOGGER.info("Callback Received: {}", Code);

            String response;
            int length;
            InputStream in;
            try{
                in = switch (MinecraftClient.getInstance().getLanguageManager().getLanguage()) {
                    case "ko_kr" -> getClass().getResourceAsStream("/allset/ko_kr.html");
                    default -> getClass().getResourceAsStream("/allset/en_us.html");
                };
                byte[] file = in.readAllBytes();
                length = file.length;
                response = new String(file);
            }catch(Exception e){
                e.printStackTrace();
                return;
            }
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();

            ApiCalls.convertAccessToken(Code);

            MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().setScreen(null));

            SongDataExtractor.reloadData(true, () -> {}, () -> {}, () -> {});
            Media.LOGGER.info("Stopping Guide Server...");
            WebGuideServer.stop();
        }
    }
    private static class configHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String[] Queries = exchange.getRequestURI().getQuery().split("&");
            String Id = "";
            String Secret = "";

            for(String pair : Queries) {
                String[] kv = pair.split("=");
                if(kv.length != 2) continue;
                if(kv[0].equalsIgnoreCase("id")) Id = kv[1];
                else if(kv[0].equalsIgnoreCase("secret")) Secret = kv[1];
            }
            if(Id.length()==0 || Secret.length()==0) {
                String response = "Bad Request";
                exchange.sendResponseHeaders(400, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            Media.LOGGER.info("Client ID Received");
            MediaClient.CONFIG.clientId(Id);
            MediaClient.CONFIG.clientSecret(Secret);

            String response = "Received";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}

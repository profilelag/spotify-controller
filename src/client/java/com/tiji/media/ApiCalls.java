package com.tiji.media;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

public class ApiCalls {
    public static void convertAccessToken(String accessToken) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://accounts.spotify.com/api/token?grant_type=authorization_code&redirect_uri=http://localhost:25566/callback&code=" + accessToken))
                .timeout(java.time.Duration.ofSeconds(2))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", getAuthorizationHeader())
                .POST(HttpRequest.BodyPublishers.noBody()).build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(body -> {
                    JsonObject data = new Gson().fromJson(body, JsonObject.class);

                    MediaClient.CONFIG.authToken(data.get("access_token").getAsString());
                    MediaClient.CONFIG.refreshToken(data.get("refresh_token").getAsString());
                    MediaClient.CONFIG.lastRefresh(System.currentTimeMillis());
                }).join();
    }
    public static void refreshAccessToken() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
               .uri(URI.create("https://accounts.spotify.com/api/token?grant_type=refresh_token&refresh_token=" + MediaClient.CONFIG.refreshToken()))
               .timeout(java.time.Duration.ofSeconds(2))
               .header("Content-Type", "application/x-www-form-urlencoded")
               .header("Authorization", getAuthorizationHeader())
               .POST(HttpRequest.BodyPublishers.noBody()).build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
               .thenApply(HttpResponse::body)
                .thenAccept(body -> {
                    JsonObject data = new Gson().fromJson(body, JsonObject.class);

                    if (data.has("error")) {
                        Media.LOGGER.warn("Failed to refresh access token; Normally caused when developer app is deleted.");
                        MediaClient.CONFIG.reset();
                        try {
                            WebGuideServer.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return;
                    }

                    MediaClient.CONFIG.authToken(data.get("access_token").getAsString());
                    if (data.has("refresh_token")) MediaClient.CONFIG.refreshToken(data.get("refresh_token").getAsString());
                    MediaClient.CONFIG.lastRefresh(System.currentTimeMillis());
                }).join();
    }
    public static JsonObject getNowPlayingTrack() {
        AtomicReference<JsonObject> returns = new AtomicReference<>();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.spotify.com/v1/me/player"))
                .timeout(Duration.ofSeconds(2))
                .header("Authorization", getAuthorizationCode())
                .GET().build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
               .thenApply(HttpResponse::body)
                .thenAccept(body -> returns.set(new Gson().fromJson(body, JsonObject.class))).join();
        return returns.get();
    }
    public static void setPlaybackLoc(int position_ms) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
               .uri(URI.create("https://api.spotify.com/v1/me/player/seek?position_ms=" + position_ms))
               .timeout(Duration.ofSeconds(2))
               .header("Authorization", getAuthorizationCode())
               .header("Content-Type", "application/json")
               .PUT(HttpRequest.BodyPublishers.noBody())
               .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
               .thenAccept(body -> {
                   if (body.statusCode()!= 204 && body.statusCode()!= 200) {
                       Media.LOGGER.warn("Failed to set playback location.");
                   }
                }).join();
    }
    public static void playPause(boolean state) {
        URI uri;
        if (state) {
            uri = URI.create("https://api.spotify.com/v1/me/player/play");
        }else {
            uri = URI.create("https://api.spotify.com/v1/me/player/pause");
        }
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(2))
                .header("Authorization", getAuthorizationCode())
                .PUT(HttpRequest.BodyPublishers.noBody()).build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(body -> {
                    // TODO
                }).join();
    }
    public static void nextTrack() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
               .uri(URI.create("https://api.spotify.com/v1/me/player/next"))
               .timeout(Duration.ofSeconds(2))
               .header("Authorization", getAuthorizationCode())
               .POST(HttpRequest.BodyPublishers.noBody()).build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
               .thenAccept(body -> {
                    if (body.statusCode() != 204 && body.statusCode() != 200) {
                        Media.LOGGER.warn("Failed to play next track: " + body);
                    }
                }).join();
    }
    public static void previousTrack() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
               .uri(URI.create("https://api.spotify.com/v1/me/player/previous"))
               .timeout(Duration.ofSeconds(2))
               .header("Authorization", getAuthorizationCode())
               .POST(HttpRequest.BodyPublishers.noBody()).build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
               .thenAccept(body -> {
                   if (body.statusCode() != 204 && body.statusCode() != 200) {
                       Media.LOGGER.warn("Failed to play previous track: " + body);
                   }
                }).join();
    }
    public static void setRepeatMode(String state) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
               .uri(URI.create("https://api.spotify.com/v1/me/player/repeat"))
               .timeout(Duration.ofSeconds(2))
               .header("Authorization", getAuthorizationCode())
               .header("Content-Type", "application/json")
               .PUT(HttpRequest.BodyPublishers.ofString("{\"state\": \"" + state + "\", \"context_uri\": null}"))
               .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
               .thenAccept(body -> {
                    // TODO
                }).join();
    }
    public static void setShuffle(boolean state) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
               .uri(URI.create("https://api.spotify.com/v1/me/player/shuffle"))
               .timeout(Duration.ofSeconds(2))
               .header("Authorization", getAuthorizationCode())
               .header("Content-Type", "application/json")
               .PUT(HttpRequest.BodyPublishers.ofString("{\"state\": " + state + "}"))
               .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
               .thenAccept(body -> {
                    // TODO
                }).join();
    }
    public static String getUserName() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
               .uri(URI.create("https://api.spotify.com/v1/me"))
               .timeout(Duration.ofSeconds(2))
               .header("Authorization", getAuthorizationCode())
               .GET().build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
               .thenApply(HttpResponse::body)
               .thenApply(body -> new Gson().fromJson(body, JsonObject.class).get("display_name").getAsString())
               .join();
    }
    private static String getAuthorizationHeader() {
        String clientId = MediaClient.CONFIG.clientId();
        String clientSecret = MediaClient.CONFIG.clientSecret();

        String encoded = java.util.Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

        return "Basic " + encoded;
    }

    private static String getAuthorizationCode() {
        return "Bearer " + MediaClient.CONFIG.authToken();
    }
}

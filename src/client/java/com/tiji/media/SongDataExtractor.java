package com.tiji.media;

import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SongDataExtractor {
    public static String getName(JsonObject trackObj) {
        return trackObj.getAsJsonObject("item").get("name").getAsString();
    }
    public static String getArtist(JsonObject trackObj) {
        StringBuilder artists = new StringBuilder();
        for (var artist : trackObj.getAsJsonObject("item").getAsJsonArray("artists")) {
            artists.append(artist.getAsJsonObject().get("name").getAsString()).append(", ");
        }
        artists.setLength(artists.length() - 2); // Remove trailing comma and space
        return artists.toString();
    }
    public static String getId(JsonObject trackObj) {
        return trackObj.getAsJsonObject("item").getAsJsonObject("album").get("id").getAsString().toLowerCase();
    }
    public static Identifier getAlbumCover(JsonObject trackObj) {
        try {
            Identifier id = Identifier.of("media", getId(trackObj));
            int wantedSize = 100 * MinecraftClient.getInstance().options.getGuiScale().getValue();
            int closest = Integer.MAX_VALUE;
            String closestUrl = trackObj.getAsJsonObject("item")
                    .getAsJsonObject("album")
                    .getAsJsonArray("images").get(0)
                    .getAsJsonObject().get("url").getAsString();

            for (int i = 0; i < trackObj.getAsJsonObject("item")
                    .getAsJsonObject("album")
                    .getAsJsonArray("images").size(); i++) {
                int size = trackObj.getAsJsonObject("item")
                        .getAsJsonObject("album")
                        .getAsJsonArray("images").get(i)
                        .getAsJsonObject().get("height").getAsInt();
                if (closest > size && size >= wantedSize) {
                    closest = size;
                    closestUrl = trackObj.getAsJsonObject("item")
                            .getAsJsonObject("album")
                            .getAsJsonArray("images").get(i)
                            .getAsJsonObject().get("url").getAsString();
                }
            }
            InputStream albumCoverUrl = new URL(closestUrl).openStream();

            // Spotify provides JPEG image that Minecraft cannot handle
            // Convert to PNG
            BufferedImage jpegImage = ImageIO.read(albumCoverUrl);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(jpegImage, "png", outputStream);
            ByteArrayInputStream imageStream = new ByteArrayInputStream(outputStream.toByteArray());

            NativeImage image = NativeImage.read(imageStream);
            MinecraftClient.getInstance().getTextureManager().registerTexture(id,
                    new NativeImageBackedTexture(image));

            return id;
        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static double getDuration(JsonObject trackObj) {
        return trackObj.get("progress_ms").getAsDouble()/
                trackObj.getAsJsonObject("item")
                        .get("duration_ms").getAsDouble();
    }
    public static String getDurationLabel(JsonObject trackObj) {
        int duration = trackObj.getAsJsonObject("item").get("duration_ms").getAsInt();

        duration /= 1000;

        Integer minutes_duration = duration / 60;
        Integer seconds_duration = duration % 60;

        return String.format("%02d:%02d", minutes_duration, seconds_duration);
    }
    public static String getProgressLabel(JsonObject trackObj) {
        int progress = trackObj.get("progress_ms").getAsInt();

        progress /= 1000;

        Integer minutes_progress = progress / 60;
        Integer seconds_progress = progress % 60;

        return String.format("%02d:%02d", minutes_progress, seconds_progress);
    }
    public static boolean isPlaying(JsonObject trackObj) {
        return trackObj.get("is_playing").getAsBoolean();
    }
    public static int getMaxDuration(JsonObject trackObj) {
        return trackObj.getAsJsonObject("item").get("duration_ms").getAsInt();
    }
    public static void reloadData(boolean forceFullReload, Consumer<JsonObject> onNoUpdate, Consumer<JsonObject> onDataUpdate, Runnable onImageLoad) {
        ApiCalls.getNowPlayingTrack(data -> {
            boolean isSongDifferent = !getId(data).equals(SongData.Id);

            SongData.progressLabel = getProgressLabel(data);
            SongData.isPlaying = isPlaying(data);
            SongData.progressValue = getDuration(data);

            if (isSongDifferent || forceFullReload) {
                SongData.title = getName(data);
                SongData.artist = getArtist(data);
                SongData.durationLabel = getDurationLabel(data);
                SongData.Id = getId(data);
                SongData.duration = getMaxDuration(data);

                if (!SongData.coverImage.getPath().equals("ui/nothing.png")) {
                    MinecraftClient.getInstance().getTextureManager().destroyTexture(SongData.coverImage);
                    SongData.coverImage = Identifier.of("media", "ui/nothing.png");
                }
                CompletableFuture<Identifier> ImageIOFuture = CompletableFuture.supplyAsync(() -> getAlbumCover(data));
                ImageIOFuture.thenAccept(id -> {
                    SongData.coverImage = id;
                    onImageLoad.run();
                });
            }
            if (isSongDifferent || forceFullReload) {
                onDataUpdate.accept(data);
            }else{
                onNoUpdate.accept(data);
            }
        });
    }
}

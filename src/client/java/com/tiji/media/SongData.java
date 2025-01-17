package com.tiji.media;

import net.minecraft.util.Identifier;

public class SongData {
    public static String title;
    public static String artist;
    public static Identifier coverImage = Identifier.of("media", "ui/nothing.png"); // Avoid NullPointerException
    public static String progressLabel;
    public static String durationLabel;
    public static Double progressValue;
    public static Integer duration;
    public static boolean isPlaying = false;
    public static String Id = "";

    public static String tostring() {
        return "songData{" +
                "title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", coverImage=" + coverImage +
                ", progress_label='" + progressLabel + '\'' +
                ", duration_label='" + durationLabel + '\'' +
                ", progress_value=" + progressValue +
                ", isPlaying=" + isPlaying +
                ", Id='" + Id + '\'' +'"' +
                '}';
    }
}

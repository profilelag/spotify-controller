package com.tiji.media;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import com.tiji.media.widgets.*;

public class NowPlayingScreen extends LightweightGuiDescription {
    private class RootPanel extends WPlainPanel {
        public void onHidden() {
            MediaClient.nowPlayingScreen = null;
        }
    }

    public WLabel songName = new WLabel(Text.translatable("ui.media.nothing_playing"));
    public WLabel artistName = new WLabel(Text.translatable("ui.media.unknown_artist"));
    public progressWidget progressBar = new progressWidget(0, 100, Axis.HORIZONTAL);
    public WLabel durationLabel = new WLabel(Text.translatable("ui.media.unknown_duration"));
    public WLabel currentTimeLabel = new WLabel(Text.translatable("ui.media.unknown_time"));
    public borderlessButtonWidget playPauseButton = new borderlessButtonWidget(Text.literal("⏸"));
    public WSprite albumCover = new WSprite(Identifier.of("media", "ui/nothing.png"));

    public NowPlayingScreen() {
        WPlainPanel root = new RootPanel();
        root.setSize(300, 200);
        root.setInsets(Insets.NONE);

        root.add(albumCover, 100, 10, 100, 100);

        root.add(new WSprite(Identifier.of("media", "ui/attribution.png")), 270, 10);

        songName = songName.setHorizontalAlignment(HorizontalAlignment.CENTER);
        root.add(songName, 100, 120, 100, 20);

        artistName = artistName.setHorizontalAlignment(HorizontalAlignment.CENTER);
        root.add(artistName, 100, 135, 100, 20);

        root.add(new borderlessButtonWidget(Text.literal("⏮")).setOnClick(ApiCalls::previousTrack), 120, 150, 20, 20);

        playPauseButton.setOnClick(() -> {
            if (SongData.Id.isEmpty()) return;

            SongData.isPlaying =! SongData.isPlaying;
            ApiCalls.playPause(SongData.isPlaying);
            playPauseButton.setLabel(Text.of(SongData.isPlaying ? "⏸" : "⏹"));});

        root.add(playPauseButton, 140, 150, 20, 20);

        root.add(new borderlessButtonWidget(Text.literal("⏭")).setOnClick(ApiCalls::nextTrack), 160, 150, 20, 20);

        currentTimeLabel = currentTimeLabel.setHorizontalAlignment(HorizontalAlignment.LEFT);
        root.add(currentTimeLabel, 10, 160, 60, 20);

        progressBar.setMaxValue(300);
        root.add(progressBar, 10, 175, 280, 10);

        durationLabel = durationLabel.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        root.add(durationLabel, 230, 160, 60, 20);

        root.validate(this);

        setRootPanel(root);
    }
    public void updateStatus() {
        if (SongData.Id.isEmpty()) return;

        if (progressBar.allowUpdateProgress) {
            if (SongData.progressValue == null) Media.LOGGER.warn("Progress value is null");
            else progressBar.setValue((int) Math.round(SongData.progressValue * 300));
        }
        currentTimeLabel.setText(Text.of(SongData.progressLabel));
        playPauseButton.setLabel(Text.of(SongData.isPlaying ? "⏸" : "⏹"));
    }
    public void updateNowPlaying() {
        if (SongData.Id.isEmpty()) {
            nothingPlaying();
            return;
        }

        Media.LOGGER.info(SongData.tostring());

        songName.setText(Text.of(SongData.title));
        artistName.setText(Text.of(SongData.artist));
        durationLabel.setText(Text.of(SongData.durationLabel));
        updateCoverImage();
        updateStatus();
    }
    public void nothingPlaying() {
        songName.setText(Text.translatable("ui.media.nothing_playing"));
        artistName.setText(Text.translatable("ui.media.unknown_artist"));
        durationLabel.setText(Text.translatable("ui.media.unknown_duration"));
        updateCoverImage();
        progressBar.setValue(0);
        currentTimeLabel.setText(Text.translatable("ui.media.unknown_time"));
    }
    public void updateCoverImage() {
        albumCover.setImage(SongData.coverImage);
    }
}

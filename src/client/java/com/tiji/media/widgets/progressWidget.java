package com.tiji.media.widgets;


import com.tiji.media.ApiCalls;
import com.tiji.media.SongData;
import io.github.cottonmc.cotton.gui.widget.WSlider;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.minecraft.client.gui.DrawContext;

public class progressWidget extends WSlider {
    public boolean allowUpdateProgress = true;

    public static final int TRACK_WIDTH = 2;

    public progressWidget(int min, int max, Axis axis) {
        super(min, max, axis);
    }

    public InputResult onMouseUp(int x, int y, int button) {
        if (SongData.Id.isEmpty()) return InputResult.PROCESSED;

        if (button == 0) {
            double progress = getValue() / 300d;
            ApiCalls.setPlaybackLoc((int) Math.round(progress * SongData.duration));
            allowUpdateProgress = true;
        }
        return super.onMouseUp(x, y, button);
    }

    public InputResult onMouseDown(int x, int y, int button) {
        if (SongData.Id.isEmpty()) return InputResult.PROCESSED;

        if (button == 0) {
            allowUpdateProgress = false;
        }
        return super.onMouseDown(x, y, button);
    }

    @Override
    public void paint(DrawContext context, int x, int y, int mouseX, int mouseY) {
        int background = shouldRenderInDarkMode() ? 0xFF555555 : 0xFFAAAAAA;
        int foreground = shouldRenderInDarkMode() ? 0xFFFFFFFF : 0xFF333333;

        context.fill(x, y, width + x, y + TRACK_WIDTH, background);
        context.fill(x, y, (int) (coordToValueRatio * (value - min)) + x, y + TRACK_WIDTH, foreground);
    }
}
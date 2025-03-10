package com.tiji.media.widgets;


import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WButton;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class borderlessButton extends WButton {
    public borderlessButton(Text text) {
        super(text);
    }

    @Override
    public void paint(DrawContext context, int x, int y, int mouseX, int mouseY) {
        int foreground = shouldRenderInDarkMode() ? 0xFFFFFFFF : 0xFF555555;
        foreground = isHovered() || isFocused() ? foreground : foreground - 0x00555555;

        ScreenDrawing.drawString(context, getLabel().asOrderedText(), alignment, x, y + ((getHeight() - 8) / 2), width, foreground);
    }
}

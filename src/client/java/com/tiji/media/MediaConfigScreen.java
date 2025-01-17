package com.tiji.media;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class MediaConfigScreen extends LightweightGuiDescription {
    public MediaConfigScreen() {
        AtomicBoolean confirmReset = new AtomicBoolean(false);

        WPlainPanel root = new WPlainPanel();
        root.setSize(200, 80);
        root.setInsets(Insets.NONE);
        
        Text statusText;
        
        if (MediaClient.isNotSetup()) {
            statusText = Text.translatable("ui.media.status.not_setup");
        }else{
            statusText = Text.translatable("ui.media.status.setup", ApiCalls.getUserName());
        }
        
        WLabel status = new WLabel(statusText);
        root.add(status, 10, 10, 180, 20);

        WButton reset = new WButton(Text.translatable("ui.media.reset_config"));
        reset.setOnClick(() -> {
            if (MediaClient.isNotSetup()) {return;}
            if (confirmReset.get()) {
                MediaClient.CONFIG.reset();

                try {
                    WebGuideServer.start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                reset.setLabel(Text.translatable("ui.media.reset_config_success"));
            }else{
                reset.setLabel(Text.translatable("ui.media.reset_config_confirm"));
                confirmReset.set(true);
            }
        });
        root.add(reset, 10, 40, 180, 20);

        root.validate(this);
        setRootPanel(root);
    }

    public abstract void close();
}

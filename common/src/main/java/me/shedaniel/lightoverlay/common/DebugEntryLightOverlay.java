package me.shedaniel.lightoverlay.common;

import static me.shedaniel.lightoverlay.common.LightOverlay.*;
import net.minecraft.client.gui.components.debug.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.*;
import org.jetbrains.annotations.*;

public class DebugEntryLightOverlay implements DebugScreenEntry {
    @Override
    public void display(DebugScreenDisplayer debugScreenDisplayer, @Nullable Level level, @Nullable LevelChunk levelChunk, @Nullable LevelChunk levelChunk2) {
        if (enabled) {
            if (caching) {
                debugScreenDisplayer.addLine(String.format("[Light Overlay] Chunks to queue: %02d", ticker.POS.size()));
            } else {
                debugScreenDisplayer.addLine("[Light Overlay] Enabled");
            }
        } else {
            debugScreenDisplayer.addLine("[Light Overlay] Disabled");
        }
    }
}

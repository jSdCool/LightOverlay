package me.shedaniel.lightoverlay.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.lightoverlay.common.*;

import java.util.function.Consumer;

public class LightOverlayImpl {
    public static LightOverlayRenderer renderer = LightOverlay.renderer;
    public static Consumer<PoseStack> debugRenderer = poses -> {};
    
    public static void init() {
        LightOverlay.register();
    }


    public static void renderWorldLast(PoseStack stack) {
        debugRenderer.accept(stack);
    }
}

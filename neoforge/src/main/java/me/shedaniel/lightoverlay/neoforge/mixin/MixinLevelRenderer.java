package me.shedaniel.lightoverlay.neoforge.mixin;

import me.shedaniel.lightoverlay.neoforge.LightOverlayImpl;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "cullTerrain", at = @At("HEAD"))
    private void setupTerrain(Camera arg, Frustum frustum, boolean bl, CallbackInfo ci) {
        LightOverlayImpl.renderer.frustum = frustum;
    }
}

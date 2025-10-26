package me.shedaniel.lightoverlay.fabric.mixin;

import me.shedaniel.lightoverlay.fabric.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "cullTerrain", at = @At("HEAD"))
    private void setupTerrain(Camera arg, Frustum frustum, boolean bl, CallbackInfo ci) {
        LightOverlayImpl.renderer.frustum = frustum;
    }
}

package me.shedaniel.lightoverlay.fabric.mixin;

import com.mojang.blaze3d.vertex.*;
import me.shedaniel.lightoverlay.fabric.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.*;
import net.minecraft.client.renderer.debug.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(DebugRenderer.class)
public class MixinDebugRenderer {
    @Inject(method = "emitGizmos", at = @At("HEAD"))
    private void render(Frustum frustum, double x, double y, double z, float g, CallbackInfo ci) {
        LightOverlayImpl.renderWorldLast(null);
    }
}

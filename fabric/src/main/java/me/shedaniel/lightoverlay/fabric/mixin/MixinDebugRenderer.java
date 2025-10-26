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
    @Inject(method = "render", at = @At("HEAD"))
    private void render(PoseStack arg, Frustum arg2, MultiBufferSource.BufferSource arg3, double d, double e, double f, boolean bl, CallbackInfo ci) {
        LightOverlayImpl.renderWorldLast(arg);
    }
}

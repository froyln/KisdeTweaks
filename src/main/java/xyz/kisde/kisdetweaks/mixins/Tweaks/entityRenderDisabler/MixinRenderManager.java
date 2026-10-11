package xyz.kisde.kisdetweaks.mixins.Tweaks.entityRenderDisabler;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.kisde.kisdetweaks.config.Configs;

@Mixin(RenderManager.class)
public abstract class MixinRenderManager {
    @Inject(method = "renderEntityStatic", at = @At("HEAD"), cancellable = true)
    private void skipHiddenEntity(Entity entity, float partialTicks, boolean hideDebugBox, CallbackInfo ci) {
        if (Configs.Generic.ENTITY_RENDER_DISABLER.getBooleanValue()
                && Configs.Generic.HIDDEN_ENTITIES.getValue().contains(entity.getClass())) {
            ci.cancel();
        }
    }
}

package xyz.kisde.kisdetweaks.mixins.Tweaks.clientTimeOverride;

import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.kisde.kisdetweaks.config.Configs;
import xyz.kisde.kisdetweaks.config.TweaksToggle;

@Mixin(WorldInfo.class)
public abstract class MixinWorldInfo {
    @Inject(
            method = "getWorldTime",
            at = @At("HEAD"),
            cancellable = true
    )
    private void clientTimeOverride(CallbackInfoReturnable<Long> cir) {
        if (TweaksToggle.CLIENT_TIME_OVERRIDE.getBooleanValue()) {
            cir.setReturnValue((long) Configs.Generic.CLIENT_TIME.getIntegerValue());
        }
    }
}

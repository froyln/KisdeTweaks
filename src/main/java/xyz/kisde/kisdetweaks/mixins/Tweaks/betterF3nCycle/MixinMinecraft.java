package xyz.kisde.kisdetweaks.mixins.Tweaks.betterF3nCycle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.kisde.kisdetweaks.config.TweaksToggle;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
    @Redirect(
            method = "processKeyF3",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;isSpectator()Z"
            )
    )
    private boolean notJustSpector(EntityPlayerSP instance) {
        if (TweaksToggle.BETTER_F3N_CYCLE.getBooleanValue()) {
            return true;
        } else {
            return instance.isSpectator();
        }
    }
}

package xyz.kisde.kisdetweaks.mixins.Tweaks.nightVision;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.kisde.kisdetweaks.config.TweaksToggle;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {
    // Only rendering sees fake vision; potion consumers must see real effects.
    @Redirect(method = "updateLightmap", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/entity/EntityPlayerSP;isPotionActive(Lnet/minecraft/potion/Potion;)Z"))
    private boolean kisdetweaks$lightmapVision(EntityPlayerSP player, Potion potion) {
        return potion == MobEffects.NIGHT_VISION && TweaksToggle.FAKE_NIGHT_VISION.getBooleanValue()
                || player.isPotionActive(potion);
    }

    @Redirect(method = "updateFogColor", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/EntityLivingBase;isPotionActive(Lnet/minecraft/potion/Potion;)Z"))
    private boolean kisdetweaks$fogVision(EntityLivingBase entity, Potion potion) {
        return potion == MobEffects.NIGHT_VISION && TweaksToggle.FAKE_NIGHT_VISION.getBooleanValue()
                || entity.isPotionActive(potion);
    }

    @Inject(
            method = "getNightVisionBrightness",
            at = @At("HEAD"),
            cancellable = true
    )
    private void alwaysNightVision(EntityLivingBase entitylivingbaseIn, float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (TweaksToggle.FAKE_NIGHT_VISION.getBooleanValue()) {
            cir.setReturnValue(1F);
        }
    }
}

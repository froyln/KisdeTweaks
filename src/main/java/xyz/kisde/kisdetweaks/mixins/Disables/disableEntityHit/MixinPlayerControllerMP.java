package xyz.kisde.kisdetweaks.mixins.Disables.disableEntityHit;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.kisde.kisdetweaks.config.DisableToggle;
import xyz.kisde.kisdetweaks.util.EntityUtils;

@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP {
    //removed due to entity list not useable in forge
    /*@Inject(
            method = "attackEntity",
            at = @At("HEAD"),
            cancellable = true
    )
    private void disableEntityHit(EntityPlayer playerIn, Entity targetEntity, CallbackInfo ci) {
        if (TweaksToggle.DISABLE_ENTITY_HIT.getBooleanValue() && EntityUtils.checkEntityBlackList(targetEntity.getClass())) {
            ci.cancel();
        }
    }*/

    @Inject(
            method = "attackEntity",
            at = @At("HEAD"),
            cancellable = true
    )
    private void disableEntityHit(EntityPlayer playerIn, Entity targetEntity, CallbackInfo ci) {
        if (DisableToggle.DISABLE_ENTITY_HIT.getBooleanValue() && EntityUtils.checkEntityBlackList(targetEntity.getClass().getName())) {
            ci.cancel();
        }
    }
}

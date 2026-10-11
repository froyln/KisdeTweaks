package xyz.kisde.kisdetweaks.mixins.Disables.disablePlayerSlowDown;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.kisde.kisdetweaks.config.DisableToggle;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends Entity {

    public MixinEntityPlayer(World worldIn) {
        super(worldIn);
    }

    @Inject(
            method = "setInWeb",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onSetInWeb(CallbackInfo ci) {
        if (DisableToggle.DISABLE_SLOW_DOWN.getBooleanValue()) {
            this.fallDistance = 0.0F;
            ci.cancel();
        }
    }
}

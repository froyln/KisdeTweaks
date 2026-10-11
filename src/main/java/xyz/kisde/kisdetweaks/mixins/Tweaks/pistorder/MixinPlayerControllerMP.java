package xyz.kisde.kisdetweaks.mixins.Tweaks.pistorder;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.kisde.kisdetweaks.util.PistonPreview;
import xyz.kisde.kisdetweaks.config.Configs;

@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP {
    @Inject(method = "processRightClickBlock(Lnet/minecraft/client/entity/EntityPlayerSP;Lnet/minecraft/client/multiplayer/WorldClient;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/EnumHand;)Lnet/minecraft/util/EnumActionResult;", at = @At("HEAD"), cancellable = true)
    private void kisdetweaks$pistonPreview(EntityPlayerSP player, WorldClient world, BlockPos pos, EnumFacing side, Vec3d hit, EnumHand hand, CallbackInfoReturnable<EnumActionResult> cir) {
        if (Configs.Generic.PISTORDER_ENABLED.getBooleanValue() && hand == EnumHand.MAIN_HAND
                && PistonPreview.handleRightClick(world, pos, player)) {
            cir.setReturnValue(EnumActionResult.SUCCESS);
        }
    }
}

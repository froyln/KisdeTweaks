package xyz.kisde.kisdetweaks.mixins.Fixes;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.kisde.kisdetweaks.config.Configs;
import xyz.kisde.kisdetweaks.util.ObserverPlacementProtocol;

@Pseudo
@Mixin(targets = "fi.dy.masa.tweakeroo.tweaks.PlacementTweaks")
public abstract class MixinTweakerooPlacement {
    @Unique private static EnumFacing kisdetweaks$observerFacing;
    @Unique private static boolean kisdetweaks$usingTick;
    @Unique private static WorldClient kisdetweaks$world;

    @Inject(method = "onUsingTick", at = @At("HEAD"), remap = false)
    private static void kisdetweaks$beginTick(CallbackInfo ci) {
        kisdetweaks$usingTick = true;
    }

    @Inject(method = "onUsingTick", at = @At("RETURN"), remap = false)
    private static void kisdetweaks$endTick(CallbackInfo ci) {
        kisdetweaks$usingTick = false;
    }

    @Inject(method = "clearClickedBlockInfoUse", at = @At("HEAD"), remap = false)
    private static void kisdetweaks$clearFacing(CallbackInfo ci) {
        kisdetweaks$observerFacing = null;
        kisdetweaks$world = null;
    }

    @Redirect(method = "processRightClickBlockWrapper", remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;processRightClickBlock(Lnet/minecraft/client/entity/EntityPlayerSP;Lnet/minecraft/client/multiplayer/WorldClient;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/EnumHand;)Lnet/minecraft/util/EnumActionResult;", ordinal = 0, remap = true))
    private static EnumActionResult kisdetweaks$keepObserverFacing(PlayerControllerMP controller,
            EntityPlayerSP player, WorldClient world, BlockPos pos, EnumFacing side, Vec3d hit, EnumHand hand) {
        if (world != kisdetweaks$world) {
            kisdetweaks$observerFacing = null;
            kisdetweaks$world = world;
        }
        if (!Configs.Fixes.FAST_OBSERVER_PLACEMENT_FIX.getBooleanValue()
                || player.getHeldItem(hand).getItem() != Item.getItemFromBlock(Blocks.OBSERVER)) {
            kisdetweaks$observerFacing = null;
            return controller.processRightClickBlock(player, world, pos, side, hit, hand);
        }
        BlockPos placement = world.getBlockState(pos).getBlock().isReplaceable(world, pos) ? pos : pos.offset(side);
        int protocol = (int) (hit.x - pos.getX());
        EnumFacing facing = protocol >= 2 ? EnumFacing.byIndex(protocol - 2)
                : EnumFacing.getDirectionFromEntityLiving(placement, player).getOpposite();
        if (kisdetweaks$usingTick && kisdetweaks$observerFacing != null) facing = kisdetweaks$observerFacing;
        // Observers use the original Carpet protocol, without after-clicker digits.
        hit = new Vec3d(pos.getX() + ObserverPlacementProtocol.encode(facing.getIndex()), hit.y, hit.z);
        EnumActionResult result = controller.processRightClickBlock(player, world, pos, side, hit, hand);
        if (result == EnumActionResult.SUCCESS && kisdetweaks$observerFacing == null) kisdetweaks$observerFacing = facing;
        return result;
    }
}

package xyz.kisde.kisdetweaks.mixins.Fixes;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.properties.IProperty;
import net.minecraft.item.ItemBlock;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
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
import xyz.kisde.kisdetweaks.util.PlacementOrientationProtocol;

@Pseudo
@Mixin(targets = "fi.dy.masa.tweakeroo.tweaks.PlacementTweaks")
public abstract class MixinTweakerooPlacement {
    @Unique private static Integer kisdetweaks$orientation;
    @Unique private static Block kisdetweaks$block;
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
        kisdetweaks$orientation = null;
        kisdetweaks$block = null;
        kisdetweaks$world = null;
    }

    @Redirect(method = "processRightClickBlockWrapper", remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;processRightClickBlock(Lnet/minecraft/client/entity/EntityPlayerSP;Lnet/minecraft/client/multiplayer/WorldClient;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/EnumHand;)Lnet/minecraft/util/EnumActionResult;", ordinal = 0, remap = true))
    private static EnumActionResult kisdetweaks$keepFacing(PlayerControllerMP controller,
            EntityPlayerSP player, WorldClient world, BlockPos pos, EnumFacing side, Vec3d hit, EnumHand hand) {
        if (world != kisdetweaks$world) {
            kisdetweaks$orientation = null;
            kisdetweaks$block = null;
            kisdetweaks$world = world;
        }
        ItemStack stack = player.getHeldItem(hand);
        Block block = stack.getItem() instanceof ItemBlock ? ((ItemBlock) stack.getItem()).getBlock()
                : stack.getItem() == Items.REPEATER ? Blocks.UNPOWERED_REPEATER
                : stack.getItem() == Items.COMPARATOR ? Blocks.UNPOWERED_COMPARATOR : null;
        if (!Configs.Fixes.FAST_OBSERVER_PLACEMENT_FIX.getBooleanValue() || !kisdetweaks$supported(block)) {
            kisdetweaks$orientation = null;
            kisdetweaks$block = null;
            return controller.processRightClickBlock(player, world, pos, side, hit, hand);
        }
        if (block != kisdetweaks$block || !kisdetweaks$usingTick) kisdetweaks$orientation = null;
        kisdetweaks$block = block;
        BlockPos placement = world.getBlockState(pos).getBlock().isReplaceable(world, pos) ? pos : pos.offset(side);
        int protocol = (int) (hit.x - pos.getX());
        if (protocol < 2) {
            IBlockState state = block.getStateForPlacement(world, placement, side,
                    (float) (hit.x - pos.getX()), (float) (hit.y - pos.getY()),
                    (float) (hit.z - pos.getZ()), stack.getMetadata(), player);
            EnumFacing facing = null;
            for (IProperty<?> property : state.getPropertyKeys()) {
                if ("facing".equals(property.getName()) && state.getValue(property) instanceof EnumFacing) {
                    facing = (EnumFacing) state.getValue(property);
                    break;
                }
            }
            if (facing == null) return controller.processRightClickBlock(player, world, pos, side, hit, hand);
            boolean upper = block instanceof BlockStairs && state.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.TOP
                    || block instanceof BlockTrapDoor && state.getValue(BlockTrapDoor.HALF) == BlockTrapDoor.DoorHalf.TOP;
            boolean open = block instanceof BlockFenceGate && state.getValue(BlockFenceGate.OPEN);
            protocol = PlacementOrientationProtocol.encode(facing.getIndex(), upper || open ? 10 : 0);
        }
        if (kisdetweaks$usingTick && kisdetweaks$orientation != null) protocol = kisdetweaks$orientation;
        hit = new Vec3d(pos.getX() + protocol, hit.y, hit.z);
        EnumActionResult result = controller.processRightClickBlock(player, world, pos, side, hit, hand);
        if (result == EnumActionResult.SUCCESS && kisdetweaks$orientation == null) kisdetweaks$orientation = protocol;
        return result;
    }

    @Unique
    private static boolean kisdetweaks$supported(Block block) {
        // Only use encodings implemented by Carpet 1.12's alternativeBlockPlacement.
        return block instanceof BlockObserver || block instanceof BlockPistonBase
                || block instanceof BlockDispenser || block instanceof BlockGlazedTerracotta
                || block instanceof BlockRedstoneDiode || block instanceof BlockTrapDoor
                || block instanceof BlockStairs || block instanceof BlockFenceGate
                || block instanceof BlockPumpkin;
    }
}

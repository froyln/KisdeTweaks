package xyz.kisde.kisdetweaks.mixins.Tweaks.placeIgnoreEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPortal;
import net.minecraft.init.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.kisde.kisdetweaks.config.TweaksToggle;
import xyz.kisde.kisdetweaks.config.Configs;

@Mixin(ItemBlock.class)
public abstract class MixinItemBlock{

    @Shadow @Final protected Block block;

    @Inject(method = "onItemUse", at = @At("HEAD"), cancellable = true)
    private void preventPortalSidePlacement(EntityPlayer player, World world, BlockPos clickedPos, EnumHand hand,
                                            EnumFacing side, float hitX, float hitY, float hitZ,
                                            CallbackInfoReturnable<EnumActionResult> cir) {
        if (!Configs.Generic.PORTAL_PLACEMENT_SAFETY.getBooleanValue()) return;

        BlockPos placePos = world.getBlockState(clickedPos).getMaterial().isReplaceable()
                ? clickedPos : clickedPos.offset(side);
        for (EnumFacing direction : EnumFacing.values()) {
            BlockPos neighbor = placePos.offset(direction);
            if (!world.isBlockLoaded(neighbor, false)) continue;
            if (world.getBlockState(neighbor).getBlock() != Blocks.PORTAL) continue;

            EnumFacing.Axis portalAxis = world.getBlockState(neighbor).getValue(BlockPortal.AXIS);
            if (direction.getAxis() == EnumFacing.Axis.Y || direction.getAxis() == portalAxis) {
                cir.setReturnValue(EnumActionResult.FAIL);
                if (player == Minecraft.getMinecraft().player) {
                    player.sendMessage(new TextComponentString("Block placement beside a nether portal is disabled"));
                }
                return;
            }
        }
    }

    @Inject(
            method = "canPlaceBlockOnSide",
            at = @At("RETURN"),
            cancellable = true
    )
    private void skipCollision(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (TweaksToggle.PLACE_IGNORE_ENTITY.getBooleanValue() && player.isCreative()) {
            cir.setReturnValue(worldIn.mayPlace(this.block, pos, true, side, player));
        }
    }

    @ModifyArg(
            method = "onItemUse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;mayPlace(Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;ZLnet/minecraft/util/EnumFacing;Lnet/minecraft/entity/Entity;)Z"
            )
    )
    private boolean skipCollisionCheck(boolean bl) {
        EntityPlayerSP sp = Minecraft.getMinecraft().player;
        return TweaksToggle.PLACE_IGNORE_ENTITY.getBooleanValue() && sp.isCreative();
    }

}

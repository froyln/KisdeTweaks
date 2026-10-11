package xyz.kisde.kisdetweaks.mixins.Fixes;

import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.kisde.kisdetweaks.util.RedstoneColors;

@Mixin(BlockRedstoneWire.class)
public abstract class MixinBlockRedstoneWire {
    @Inject(method = "colorMultiplier", at = @At("HEAD"), cancellable = true)
    private static void kisdetweaks$customColor(int power, CallbackInfoReturnable<Integer> cir) {
        int color = RedstoneColors.getColor(power);
        if (color >= 0) cir.setReturnValue(color);
    }

    @Redirect(method = "randomDisplayTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    private void kisdetweaks$customParticle(World world, EnumParticleTypes type,
            double x, double y, double z, double red, double green, double blue, int[] args,
            IBlockState state, World targetWorld, net.minecraft.util.math.BlockPos pos, java.util.Random random) {
        int color = RedstoneColors.getColor(state.getValue(BlockRedstoneWire.POWER));
        if (color >= 0) {
            // Zero red selects the vanilla particle's default red instead of our custom color.
            red = Math.max(0.0001D, ((color >> 16) & 255) / 255.0D);
            green = ((color >> 8) & 255) / 255.0D;
            blue = (color & 255) / 255.0D;
        }
        world.spawnParticle(type, x, y, z, red, green, blue, args);
    }
}

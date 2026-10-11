package xyz.kisde.kisdetweaks.mixins.Tweaks.durabilityViewer;

import net.minecraft.client.gui.GuiIngame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.kisde.kisdetweaks.util.DurabilityViewer;

@Mixin(GuiIngame.class)
public abstract class MixinGuiIngame {
    @Inject(method = "renderGameOverlay(F)V", at = @At("TAIL"))
    private void kisdetweaks$renderDurability(float partialTicks, CallbackInfo ci) {
        DurabilityViewer.render();
    }
}

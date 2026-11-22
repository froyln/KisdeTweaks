package xyz.kisde.kisdetweaks.mixins.Fixes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.kisde.kisdetweaks.config.Configs;
import xyz.kisde.kisdetweaks.util.MenuHotkeyGuard;

@Mixin(Minecraft.class)
public abstract class MixinMinecraftMenuHotkeys {
    @Shadow public GuiScreen currentScreen;

    @Inject(method = "displayGuiScreen", at = @At("HEAD"), cancellable = true, require = 1)
    private void kisdetweaks$keepOpenMenu(GuiScreen next, CallbackInfo ci) {
        if (Configs.Fixes.MALILIB_MENU_HOTKEY_FIX.getBooleanValue()
                && MenuHotkeyGuard.shouldBlock(currentScreen, next)) {
            ci.cancel();
        }
    }
}

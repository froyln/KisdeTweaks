package xyz.kisde.kisdetweaks.mixins.Fixes;

import net.minecraft.client.Minecraft;
import fi.dy.masa.malilib.input.InputDispatcherImpl;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.kisde.kisdetweaks.config.Configs;
import xyz.kisde.kisdetweaks.mixins.Accessors.GuiContainerAccessor;

// Run before MaLiLib dispatches conflicting inventory hotkeys to other mods.
@Mixin(value = InputDispatcherImpl.class, remap = false)
public abstract class MixinInputDispatcher {
    @Inject(method = "onKeyInput", at = @At("HEAD"), cancellable = true)
    private void kisdetweaks$preferHotbarKey(CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getMinecraft();
        if (Configs.Fixes.HOTBAR_HOTKEYS_FIX.getBooleanValue()
                && mc.currentScreen instanceof GuiContainer && Keyboard.getEventKeyState()
                && ((GuiContainerAccessor) mc.currentScreen).kisdetweaks$checkHotbarKeys(Keyboard.getEventKey())) {
            cir.setReturnValue(true);
        }
    }
    @Inject(method = "onMouseInput", at = @At("HEAD"), cancellable = true)
    private void kisdetweaks$preferHotbarMouse(CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getMinecraft();
        int button = Mouse.getEventButton();
        if (Configs.Fixes.HOTBAR_HOTKEYS_FIX.getBooleanValue()
                && mc.currentScreen instanceof GuiContainer && button >= 0 && Mouse.getEventButtonState()
                && ((GuiContainerAccessor) mc.currentScreen).kisdetweaks$checkHotbarKeys(button - 100)) {
            cir.setReturnValue(true);
        }
    }
}

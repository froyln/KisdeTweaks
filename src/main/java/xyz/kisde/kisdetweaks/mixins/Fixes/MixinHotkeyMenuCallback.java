package xyz.kisde.kisdetweaks.mixins.Fixes;

import fi.dy.masa.malilib.input.ActionResult;
import fi.dy.masa.malilib.input.KeyAction;
import fi.dy.masa.malilib.input.KeyBind;
import fi.dy.masa.malilib.input.KeyBindImpl;
import fi.dy.masa.malilib.input.callback.HotkeyCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.kisde.kisdetweaks.util.MenuHotkeyGuard;

@Mixin(value = KeyBindImpl.class, remap = false)
public abstract class MixinHotkeyMenuCallback {
    @Redirect(method = "triggerKeyCallback", at = @At(value = "INVOKE",
            target = "Lfi/dy/masa/malilib/input/callback/HotkeyCallback;onKeyAction(Lfi/dy/masa/malilib/input/KeyAction;Lfi/dy/masa/malilib/input/KeyBind;)Lfi/dy/masa/malilib/input/ActionResult;"),
            require = 1)
    private ActionResult kisdetweaks$trackMenuHotkey(HotkeyCallback callback, KeyAction action, KeyBind key) {
        return MenuHotkeyGuard.runCallback(callback, action, key);
    }
}

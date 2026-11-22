package xyz.kisde.kisdetweaks.util;

import fi.dy.masa.malilib.gui.BaseScreen;
import fi.dy.masa.malilib.input.ActionResult;
import fi.dy.masa.malilib.input.KeyAction;
import fi.dy.masa.malilib.input.KeyBind;
import fi.dy.masa.malilib.input.callback.HotkeyCallback;
import net.minecraft.client.gui.GuiScreen;

public final class MenuHotkeyGuard {
    private static final ThreadLocal<Boolean> IN_CALLBACK = new ThreadLocal<>();

    private MenuHotkeyGuard() {}

    public static ActionResult runCallback(HotkeyCallback callback, KeyAction action, KeyBind key) {
        if (Boolean.TRUE.equals(IN_CALLBACK.get())) {
            return callback.onKeyAction(action, key);
        }

        IN_CALLBACK.set(true);
        try {
            return callback.onKeyAction(action, key);
        } finally {
            IN_CALLBACK.remove();
        }
    }

    public static boolean shouldBlock(GuiScreen current, GuiScreen next) {
        return current instanceof BaseScreen && next instanceof BaseScreen
                && Boolean.TRUE.equals(IN_CALLBACK.get())
                && next != ((BaseScreen) current).getParent();
    }
}

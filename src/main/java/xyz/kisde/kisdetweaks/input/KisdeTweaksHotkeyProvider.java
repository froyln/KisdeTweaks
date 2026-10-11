package xyz.kisde.kisdetweaks.input;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.input.Hotkey;
import fi.dy.masa.malilib.input.HotkeyCategory;
import fi.dy.masa.malilib.input.HotkeyProvider;
import xyz.kisde.kisdetweaks.Reference;
import xyz.kisde.kisdetweaks.config.Configs;
import xyz.kisde.kisdetweaks.config.DisableToggle;
import xyz.kisde.kisdetweaks.config.Hotkeys;
import xyz.kisde.kisdetweaks.config.TweaksToggle;

import java.sql.Ref;
import java.util.List;

public class KisdeTweaksHotkeyProvider implements HotkeyProvider {
    public static final KisdeTweaksHotkeyProvider INSTANCE = new KisdeTweaksHotkeyProvider();
    /**
     * Returns a list of all hotkeys that should be registered.
     * This is called when the master hotkey list in malilib is being rebuilt,
     * Any hotkeys not on the returned list will not function!
     */
    @Override
    public List<? extends Hotkey> getAllHotkeys() {
        ImmutableList.Builder<Hotkey> builder = ImmutableList.builder();

        builder.addAll(Configs.Generic.OPTIONS_HOTKEY);
        builder.addAll(Hotkeys.HOTKEY_LIST);
        builder.addAll(TweaksToggle.TOGGLE_HOTKEYS);
        builder.addAll(DisableToggle.TOGGLE_HOTKEYS);

        return builder.build();
    }

    /**
     * Returns a list of all the hotkeys, grouped in categories.
     * This is mostly just used for the keybind overlap info hover text.
     */
    @Override
    public List<HotkeyCategory> getHotkeysByCategories() {
        return ImmutableList.of(
                new HotkeyCategory(Reference.MOD_INFO, "kisdetweaks.hotkeys.category.generic", Configs.Generic.OPTIONS_HOTKEY),
                new HotkeyCategory(Reference.MOD_INFO, "kisdetweaks.hotkeys.category.generic_hotkeys", Hotkeys.HOTKEY_LIST),
                new HotkeyCategory(Reference.MOD_INFO, "kisdetweaks.hotkeys.category.tweak_toggle_hotkeys", TweaksToggle.TOGGLE_HOTKEYS),
                new HotkeyCategory(Reference.MOD_INFO, "kisdetweaks.hotkeys.category.disable_toggle_hotkeys", DisableToggle.TOGGLE_HOTKEYS)
        );
    }
}

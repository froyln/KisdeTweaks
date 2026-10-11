package xyz.kisde.kisdetweaks.config;

import fi.dy.masa.malilib.action.Action;
import fi.dy.masa.malilib.action.ActionUtils;
import fi.dy.masa.malilib.action.NamedAction;
import fi.dy.masa.malilib.action.ParameterizedAction;
import fi.dy.masa.malilib.listener.EventListener;
import xyz.kisde.kisdetweaks.Reference;
import xyz.kisde.kisdetweaks.gui.ConfigScreen;
import xyz.kisde.kisdetweaks.util.AntiGhostBlock;
import xyz.kisde.kisdetweaks.util.RandomUtils;
import xyz.kisde.kisdetweaks.util.RefreshInventory;
import xyz.kisde.kisdetweaks.util.autoInventoryProcess.DropPlayerInventory;

public class Actions {
    public static final NamedAction OPEN_CONFIG_GUI = register("openConfigScreen", ConfigScreen::open);
    public static final NamedAction CLEAR_GHOST_BLOCK = register("clearGhostBlock", AntiGhostBlock::clearGBLeftWithMsg);
    public static final NamedAction CLEAR_GHOST_BLOCK_RC = register("clearGhostBlockRightClick", AntiGhostBlock::clearGBRightWithMsg);
    public static final NamedAction REFRESH_INVENTORY  = register("refreshInventory", RefreshInventory::refreshInv);
    public static final NamedAction TOGGLE_MAIN_HAND = register("toggleMainHand", RandomUtils::swapMainHand);
    public static final NamedAction SWAP_BLOCK = register("swapBlock", RandomUtils::swapBlock);
    public static final NamedAction TOGGLE_ELYTRA_FLYING = register("stopElytraFlying", RandomUtils::toggleElytraFlying);
    public static final NamedAction DROP_INVENTORY = register("dropInventory", DropPlayerInventory::dropInventory);

    public static void init() {
        for (TweaksToggle tweak : TweaksToggle.VALUES) {
            ActionUtils.registerBooleanConfigActions(Reference.MOD_INFO, tweak.getBooleanConfig(), tweak.getKeyBind());
        }

        for (DisableToggle disable : DisableToggle.VALUES) {
            ActionUtils.registerBooleanConfigActions(Reference.MOD_INFO, disable.getBooleanConfig(), disable.getKeyBind());
        }

        ActionUtils.registerBooleanConfigActions(Configs.Generic.OPTIONS);
    }

    //register
    private static NamedAction register(String name, EventListener action) {
        return ActionUtils.register(Reference.MOD_INFO, name, action);
    }

    private static NamedAction register(String name, Action action) {
        return ActionUtils.register(Reference.MOD_INFO, name, action);
    }

    private static NamedAction register(String name, ParameterizedAction action) {
        return ActionUtils.register(Reference.MOD_INFO, name, action);
    }


}

package xyz.kisde.kisdetweaks.config;

import fi.dy.masa.malilib.config.JsonModConfig;
import fi.dy.masa.malilib.event.InitializationHandler;
import fi.dy.masa.malilib.event.dispatch.InitializationDispatcherImpl;
import fi.dy.masa.malilib.gui.config.ConfigSearchInfo;
import fi.dy.masa.malilib.registry.Registry;
import xyz.kisde.kisdetweaks.Reference;
import xyz.kisde.kisdetweaks.config.option.EntityListConfig;
import xyz.kisde.kisdetweaks.config.option.EntityListConfigWidget;
import xyz.kisde.kisdetweaks.gui.ConfigScreen;
import xyz.kisde.kisdetweaks.gui.DisableToggleConfigWidget;
import xyz.kisde.kisdetweaks.gui.TweaksToggleConfigWidget;
import xyz.kisde.kisdetweaks.gui.info.DisableConfigStatusWidget;
import xyz.kisde.kisdetweaks.gui.info.TweakConfigStatusWidget;
import xyz.kisde.kisdetweaks.input.KeyboardInputHandlerImpl;
import xyz.kisde.kisdetweaks.input.KisdeTweaksHotkeyProvider;
import xyz.kisde.kisdetweaks.input.MouseInputHandlerImpl;

public class InitHandler implements InitializationHandler {
    /**
     * This method will be called for any registered {@link InitializationHandler}
     * when the game has been initialized and the mods can register their keybinds and configs
     * to malilib without causing class loading issues.
     * <br><br>
     * So call all your (malilib-facing) mod init stuff inside this handler!
     * <br><br>
     * The classes implementing this method should be registered to {@link InitializationDispatcherImpl}
     */
    @Override
    public void registerModHandlers() {
        //cfgs
        Registry.CONFIG_MANAGER.registerConfigHandler(new JsonModConfig(Reference.MOD_INFO, Configs.CURRENT_VERSION, Configs.CATEGORIES));

        Registry.CONFIG_SCREEN.registerConfigScreenFactory(Reference.MOD_INFO, ConfigScreen::create);
        Registry.CONFIG_TAB.registerConfigTabProvider(Reference.MOD_INFO, ConfigScreen::getConfigTabs);

        //widgets
        //tweaks
        Registry.CONFIG_WIDGET.registerConfigWidgetFactory(TweaksToggle.class, TweaksToggleConfigWidget::new);
        Registry.CONFIG_WIDGET.registerConfigSearchInfo(TweaksToggle.class, new ConfigSearchInfo<TweaksToggle>(true, true)
                .setBooleanStorageGetter(TweaksToggle::getBooleanConfig)
                .setKeyBindGetter(TweaksToggle::getKeyBind));
        Registry.CONFIG_STATUS_WIDGET.registerConfigStatusWidgetFactory(TweaksToggle.class, TweakConfigStatusWidget::new, "kisdetweaks:csi_value_tweak_toggle");
        //disables
        Registry.CONFIG_WIDGET.registerConfigWidgetFactory(DisableToggle.class, DisableToggleConfigWidget::new);
        Registry.CONFIG_WIDGET.registerConfigSearchInfo(DisableToggle.class, new ConfigSearchInfo<DisableToggle>(true, true)
                .setBooleanStorageGetter(DisableToggle::getBooleanConfig)
                .setKeyBindGetter(DisableToggle::getKeyBind));
        Registry.CONFIG_STATUS_WIDGET.registerConfigStatusWidgetFactory(DisableToggle.class, DisableConfigStatusWidget::new, "kisdetweaks:csi_value_disable_toggle");

        //EntityListConfig
        Registry.CONFIG_WIDGET.registerConfigWidgetFactory(EntityListConfig.class, EntityListConfigWidget::new);

        //input
        Registry.HOTKEY_MANAGER.registerHotkeyProvider(KisdeTweaksHotkeyProvider.INSTANCE);
        //Haven't use these two yet, so just leave them here?
        //Registry.INPUT_DISPATCHER.registerKeyboardInputHandler(KeyboardInputHandlerImpl.INSTANCE);
        Registry.INPUT_DISPATCHER.registerMouseClickHandler(new MouseInputHandlerImpl());

        //hotkey callback
        Actions.init();
        Callbacks.init();
    }
}

package xyz.kisde.kisdetweaks.gui.info;

import fi.dy.masa.malilib.config.option.HotkeyedBooleanConfig;
import fi.dy.masa.malilib.overlay.widget.sub.HotkeyedBooleanConfigStatusWidget;
import fi.dy.masa.malilib.util.data.ConfigOnTab;
import xyz.kisde.kisdetweaks.config.DisableToggle;

public class DisableConfigStatusWidget extends HotkeyedBooleanConfigStatusWidget {
    public DisableConfigStatusWidget(DisableToggle config, ConfigOnTab configOnTab) {
        super(config.getBooleanConfig(), config::getKeyBind, configOnTab, "kisdetweaks:csi_value_disable_toggle");
    }
}

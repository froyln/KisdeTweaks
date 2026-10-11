package xyz.kisde.kisdetweaks.gui;

import fi.dy.masa.malilib.config.option.BooleanConfig;
import fi.dy.masa.malilib.gui.config.ConfigWidgetContext;
import fi.dy.masa.malilib.gui.widget.list.entry.DataListEntryWidgetData;
import fi.dy.masa.malilib.gui.widget.list.entry.config.BaseHotkeyedBooleanConfigWidget;
import fi.dy.masa.malilib.input.KeyBind;
import xyz.kisde.kisdetweaks.config.TweaksToggle;

public class TweaksToggleConfigWidget extends BaseHotkeyedBooleanConfigWidget<TweaksToggle> {

    public TweaksToggleConfigWidget(TweaksToggle baseConfig,
                                    DataListEntryWidgetData constructData,
                                    ConfigWidgetContext ctx) {
        super(baseConfig, baseConfig.getBooleanConfig(), baseConfig.getKeyBind(), constructData, ctx);
    }
}

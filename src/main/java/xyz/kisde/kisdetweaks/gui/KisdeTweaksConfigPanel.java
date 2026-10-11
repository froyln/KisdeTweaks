package xyz.kisde.kisdetweaks.gui;

import fi.dy.masa.malilib.gui.config.liteloader.RedirectingConfigPanel;

public class KisdeTweaksConfigPanel extends RedirectingConfigPanel {
    public KisdeTweaksConfigPanel() {
        super(ConfigScreen::create);
    }
}

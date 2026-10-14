package xyz.kisde.kisdetweaks.gui;

import fi.dy.masa.malilib.registry.Registry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import xyz.kisde.kisdetweaks.Reference;
import xyz.kisde.kisdetweaks.config.Configs;
import xyz.kisde.kisdetweaks.util.DurabilityViewer;

import java.io.IOException;

public class DurabilityHudPositionScreen extends GuiScreen {
    private final GuiScreen parent;
    private int[] bounds;
    private boolean dragging;
    private int lastX;
    private int lastY;

    public DurabilityHudPositionScreen(GuiScreen parent) {
        this.parent = parent;
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        bounds = DurabilityViewer.renderPreview();
        drawRect(bounds[0] - 2, bounds[1] - 2, bounds[2] + 2, bounds[1] - 1, 0xFF55FFFF);
        drawRect(bounds[0] - 2, bounds[3] + 1, bounds[2] + 2, bounds[3] + 2, 0xFF55FFFF);
        drawRect(bounds[0] - 2, bounds[1] - 2, bounds[0] - 1, bounds[3] + 2, 0xFF55FFFF);
        drawRect(bounds[2] + 1, bounds[1] - 2, bounds[2] + 2, bounds[3] + 2, 0xFF55FFFF);
        drawCenteredString(fontRenderer, I18n.format("kisdetweaks.message.move_durability_hud"), width / 2, 12, 0xFFFFFF);
    }

    @Override protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0 && bounds != null && mouseX >= bounds[0] - 2 && mouseX <= bounds[2] + 2
                && mouseY >= bounds[1] - 2 && mouseY <= bounds[3] + 2) {
            dragging = true;
            lastX = mouseX;
            lastY = mouseY;
        }
    }

    @Override protected void mouseClickMove(int mouseX, int mouseY, int button, long elapsed) {
        if (!dragging || button != 0) return;
        int dx = Math.max(2 - bounds[0], Math.min(width - 2 - bounds[2], mouseX - lastX));
        int dy = Math.max(2 - bounds[1], Math.min(height - 2 - bounds[3], mouseY - lastY));
        String corner = Configs.Generic.DURABILITY_HUD_CORNER.getValue().getName();
        Configs.Generic.DURABILITY_HUD_X_OFFSET.setIntegerValue(Configs.Generic.DURABILITY_HUD_X_OFFSET.getIntegerValue() + (corner.endsWith("left") ? dx : -dx));
        Configs.Generic.DURABILITY_HUD_Y_OFFSET.setIntegerValue(Configs.Generic.DURABILITY_HUD_Y_OFFSET.getIntegerValue() + (corner.startsWith("top") ? dy : -dy));
        lastX = mouseX;
        lastY = mouseY;
    }

    @Override protected void mouseReleased(int mouseX, int mouseY, int state) {
        dragging = false;
    }

    @Override protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) mc.displayGuiScreen(parent);
        else super.keyTyped(typedChar, keyCode);
    }

    @Override public void onGuiClosed() {
        Registry.CONFIG_MANAGER.saveConfigsIfChanged(Reference.MOD_INFO);
    }
}

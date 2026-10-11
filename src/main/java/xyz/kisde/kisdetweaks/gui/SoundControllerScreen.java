package xyz.kisde.kisdetweaks.gui;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import org.lwjgl.input.Mouse;
import xyz.kisde.kisdetweaks.config.Configs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SoundControllerScreen extends GuiScreen {
    private final List<ResourceLocation> sounds = new ArrayList<>();
    private GuiTextField search;
    private int scroll;

    @Override public void initGui() {
        sounds.clear();
        sounds.addAll(SoundEvent.REGISTRY.getKeys());
        Collections.sort(sounds, (a, b) -> a.toString().compareToIgnoreCase(b.toString()));
        search = new GuiTextField(0, fontRenderer, width / 2 - 150, 25, 300, 18);
        search.setFocused(true);
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, "Sound volume controls", width / 2, 8, 0xFFFFFF);
        search.drawTextBox();
        int row = 0;
        for (ResourceLocation id : sounds) {
            if (!matches(id)) continue;
            int y = 50 + row++ * 18 - scroll;
            if (y < 42 || y > height - 20) continue;
            int left = width - 160, right = width - 35;
            drawString(fontRenderer, fontRenderer.trimStringToWidth(id.toString(), left - 18), 12, y + 4, 0xFFFFFF);
            drawRect(left, y + 4, right, y + 14, 0xFF404040);
            int volume = (int) (getGain(id) * 100);
            drawRect(left, y + 4, left + (right - left) * volume / 100, y + 14, 0xFF55AA55);
            drawString(fontRenderer, volume + "%", right + 5, y + 4, 0xFFFFFF);
        }
        drawCenteredString(fontRenderer, "Click a bar to set volume; Esc to close", width / 2, height - 12, 0xAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) scroll = Math.max(0, Math.min(Math.max(0, visibleCount() * 18 - (height - 70)), scroll - Integer.signum(wheel) * 54));
    }

    @Override protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseY >= 25 && mouseY < 43) search.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0 && mouseY >= 50 && mouseY < height - 18) {
            int target = (mouseY - 50 + scroll) / 18;
            int index = 0;
            for (ResourceLocation id : sounds) {
                if (!matches(id)) continue;
                if (index++ != target) continue;
                int left = width - 160, right = width - 35;
                if (mouseX >= left && mouseX <= right) setGain(id, Math.max(0F, Math.min(1F, (mouseX - left) / (float) (right - left))));
                break;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (search.textboxKeyTyped(typedChar, keyCode)) { scroll = 0; return; }
        super.keyTyped(typedChar, keyCode);
    }

    private boolean matches(ResourceLocation id) {
        return search == null || search.getText().isEmpty() || id.toString().toLowerCase().contains(search.getText().toLowerCase());
    }

    private int visibleCount() {
        int count = 0;
        for (ResourceLocation id : sounds) if (matches(id)) count++;
        return count;
    }

    private float getGain(ResourceLocation id) {
        String prefix = id + "=";
        for (String entry : Configs.Generic.SOUND_GAINS.getValue()) {
            if (entry.startsWith(prefix)) try { return Float.parseFloat(entry.substring(prefix.length())); } catch (NumberFormatException ignored) { return 1F; }
        }
        return 1F;
    }

    private void setGain(ResourceLocation id, float gain) {
        String prefix = id + "=";
        List<String> entries = new ArrayList<>();
        for (String entry : Configs.Generic.SOUND_GAINS.getValue()) if (!entry.startsWith(prefix)) entries.add(entry);
        if (gain < 0.999F) entries.add(prefix + String.format(java.util.Locale.ROOT, "%.2f", gain));
        Configs.Generic.SOUND_GAINS.setValue(ImmutableList.copyOf(entries));
    }
}

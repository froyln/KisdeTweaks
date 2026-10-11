package xyz.kisde.kisdetweaks.mixins.Tweaks.commandCompletion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.play.client.CPacketTabComplete;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.kisde.kisdetweaks.config.Configs;

@Mixin(GuiChat.class)
public abstract class MixinGuiChat {
    @Shadow protected GuiTextField inputField;

    @Unique private final List<String> kisdetweaks$suggestions = new ArrayList<>();
    @Unique private int kisdetweaks$selected;
    @Unique private String kisdetweaks$lastInput = "";
    @Unique private String kisdetweaks$requestedInput = "";

    @Inject(method = "setCompletions", at = @At("HEAD"), cancellable = true)
    private void collectCompletions(String[] completions, CallbackInfo ci) {
        kisdetweaks$suggestions.clear();
        if (!Configs.Generic.COMMAND_COMPLETION_ENABLED.getBooleanValue()) return;
        if (!inputField.getText().equals(kisdetweaks$requestedInput)) {
            ci.cancel();
            return;
        }
        for (String suggestion : completions) {
            if (kisdetweaks$suggestions.size() == 8) break;
            if (!suggestion.isEmpty() && !kisdetweaks$suggestions.contains(suggestion)) {
                kisdetweaks$suggestions.add(suggestion);
            }
        }
        String input = inputField.getText();
        if (input.startsWith("/cal") && input.indexOf(' ') < 0 && !kisdetweaks$suggestions.contains("/calc")) {
            kisdetweaks$suggestions.add("/calc");
        }
        kisdetweaks$selected = 0;
        kisdetweaks$lastInput = input;
        ci.cancel();
    }

    @Inject(method = "keyTyped(CI)V", at = @At("HEAD"), cancellable = true)
    private void handleSuggestionKeys(char typedChar, int keyCode, CallbackInfo ci) throws IOException {
        if (!Configs.Generic.COMMAND_COMPLETION_ENABLED.getBooleanValue()) return;
        boolean up = keyCode == Keyboard.KEY_UP;
        boolean down = keyCode == Keyboard.KEY_DOWN;
        if ((up || down) && (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))) {
            kisdetweaks$suggestions.clear();
            return;
        }
        if (up || down) {
            if (!kisdetweaks$suggestions.isEmpty()) {
                int direction = up ? 1 : -1;
                kisdetweaks$selected = (kisdetweaks$selected + direction + kisdetweaks$suggestions.size()) % kisdetweaks$suggestions.size();
                ci.cancel();
            }
            return;
        }
        if (keyCode == 15 && !kisdetweaks$suggestions.isEmpty()) {
            String suggestion = kisdetweaks$suggestions.get(kisdetweaks$selected);
            String input = inputField.getText();
            int cursor = inputField.getCursorPosition();
            int tokenStart = inputField.getNthWordFromPosWS(-1, cursor, false);
            String replacement = input.substring(0, tokenStart) + suggestion + input.substring(cursor);
            inputField.setText(replacement);
            inputField.setCursorPosition(tokenStart + suggestion.length());
            kisdetweaks$lastInput = replacement;
            ci.cancel();
        }
    }

    @Inject(method = "keyTyped(CI)V", at = @At("TAIL"))
    private void clearStaleSuggestions(char typedChar, int keyCode, CallbackInfo ci) {
        if (keyCode != 15 && keyCode != 200 && keyCode != 208
                && !inputField.getText().equals(kisdetweaks$lastInput)) {
            kisdetweaks$suggestions.clear();
            String input = inputField.getText();
            if (input.startsWith("/cal") && input.indexOf(' ') < 0) {
                kisdetweaks$suggestions.add("/calc");
            }
            kisdetweaks$selected = 0;
            kisdetweaks$lastInput = input;
            if (input.startsWith("/") && Minecraft.getMinecraft().player != null
                    && Minecraft.getMinecraft().player.connection != null) {
                kisdetweaks$requestedInput = input;
                Minecraft.getMinecraft().player.connection.sendPacket(new CPacketTabComplete(
                        input.substring(0, inputField.getCursorPosition()), null, false));
            }
        }
    }

    @Inject(method = "drawScreen(IIF)V", at = @At("TAIL"))
    private void drawSuggestions(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (!Configs.Generic.COMMAND_COMPLETION_ENABLED.getBooleanValue()) return;
        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        int width = resolution.getScaledWidth();
        int height = resolution.getScaledHeight();
        int rows = Math.min(8, kisdetweaks$suggestions.size());
        int x = 4;
        int bottom = height - 14;
        for (int row = 0; row < rows; row++) {
            int index = row;
            int y = bottom - (row + 1) * 12;
            int color = index == kisdetweaks$selected ? 0xE0606070 : 0xC0202020;
            GuiChat.drawRect(x, y, Math.min(width - 4, x + 220), y + 12, color);
            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(kisdetweaks$suggestions.get(index), x + 4, y + 2, 0xFFFFFF);
        }
    }

    @Inject(method = "mouseClicked(III)V", at = @At("HEAD"), cancellable = true)
    private void clickSuggestion(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) throws IOException {
        if (!Configs.Generic.COMMAND_COMPLETION_ENABLED.getBooleanValue()) return;
        if (mouseButton != 0 || kisdetweaks$suggestions.isEmpty()) return;
        int height = new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight();
        int row = (height - 15 - mouseY) / 12;
        int rows = Math.min(8, kisdetweaks$suggestions.size());
        if (row < 0 || row >= rows || mouseX < 4 || mouseX > 224) return;
        String suggestion = kisdetweaks$suggestions.get(row);
        String input = inputField.getText();
        int cursor = inputField.getCursorPosition();
        int tokenStart = inputField.getNthWordFromPosWS(-1, cursor, false);
        String replacement = input.substring(0, tokenStart) + suggestion + input.substring(cursor);
        inputField.setText(replacement);
        inputField.setCursorPosition(tokenStart + suggestion.length());
        kisdetweaks$lastInput = replacement;
        kisdetweaks$suggestions.clear();
        ci.cancel();
    }
}

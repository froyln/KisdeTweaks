package xyz.kisde.kisdetweaks.mixins.Tweaks.commandCompletion;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.play.client.CPacketTabComplete;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.kisde.kisdetweaks.config.Configs;
import xyz.kisde.kisdetweaks.util.ChatCompletion;

@Mixin(GuiChat.class)
public abstract class MixinGuiChat {
    @Shadow protected GuiTextField inputField;
    @Unique private final ChatCompletion kisdetweaks$completion = new ChatCompletion();

    @Unique
    private void kisdetweaks$refresh() {
        if (!Configs.Generic.COMMAND_COMPLETION_ENABLED.getBooleanValue()) return;
        kisdetweaks$completion.configure(Configs.Generic.COMMAND_COMPLETION_MODE.getValue().mode,
                Configs.Generic.COMMAND_COMPLETION_CASE_SENSITIVE.getBooleanValue(),
                Configs.Generic.COMMAND_COMPLETION_HIDDEN_PREFIX.getValue(),
                Configs.Generic.COMMAND_COMPLETION_NBT.getBooleanValue());
        kisdetweaks$completion.update(inputField.getText(), inputField.getCursorPosition(),
                Configs.Generic.CALCULATOR_ENABLED.getBooleanValue());
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.player.connection == null) return;
        String query = kisdetweaks$completion.request();
        if (query != null) {
            BlockPos target = mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK
                    ? mc.objectMouseOver.getBlockPos() : null;
            mc.player.connection.sendPacket(new CPacketTabComplete(query, target, false));
        }
    }

    @Inject(method = "setCompletions", at = @At("HEAD"), cancellable = true)
    private void collectCompletions(String[] completions, CallbackInfo ci) {
        if (!Configs.Generic.COMMAND_COMPLETION_ENABLED.getBooleanValue()) return;
        if (!inputField.getText().startsWith("/") && !kisdetweaks$completion.pending()) return;
        kisdetweaks$completion.configure(Configs.Generic.COMMAND_COMPLETION_MODE.getValue().mode,
                Configs.Generic.COMMAND_COMPLETION_CASE_SENSITIVE.getBooleanValue(),
                Configs.Generic.COMMAND_COMPLETION_HIDDEN_PREFIX.getValue(),
                Configs.Generic.COMMAND_COMPLETION_NBT.getBooleanValue());
        kisdetweaks$completion.update(inputField.getText(), inputField.getCursorPosition(),
                Configs.Generic.CALCULATOR_ENABLED.getBooleanValue());
        kisdetweaks$completion.receive(completions, Configs.Generic.CALCULATOR_ENABLED.getBooleanValue());
        kisdetweaks$refresh();
        ci.cancel();
    }

    @Inject(method = "keyTyped(CI)V", at = @At("HEAD"), cancellable = true)
    private void handleSuggestionKeys(char typedChar, int keyCode, CallbackInfo ci) throws IOException {
        if (!Configs.Generic.COMMAND_COMPLETION_ENABLED.getBooleanValue()) return;
        // Preserve vanilla player-name completion for ordinary chat.
        if (!inputField.getText().startsWith("/")) {
            if (keyCode == Keyboard.KEY_TAB && kisdetweaks$completion.pending()) ci.cancel();
            return;
        }
        kisdetweaks$refresh();
        boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        if (keyCode == Keyboard.KEY_TAB) {
            kisdetweaks$apply(shift);
            ci.cancel();
        } else if (shift && (keyCode == Keyboard.KEY_UP || keyCode == Keyboard.KEY_DOWN)
                && !kisdetweaks$completion.suggestions().isEmpty()) {
            kisdetweaks$completion.move(keyCode == Keyboard.KEY_UP ? 1 : -1);
            ci.cancel();
        }
    }

    @Unique
    private void kisdetweaks$apply(boolean backwards) {
        String text = kisdetweaks$completion.apply(backwards);
        if (text != null) {
            inputField.setText(text);
            inputField.setCursorPosition(kisdetweaks$completion.cursor());
        }
    }

    @Inject(method = "keyTyped(CI)V", at = @At("TAIL"))
    private void refreshAfterKey(char typedChar, int keyCode, CallbackInfo ci) {
        kisdetweaks$refresh();
    }

    @Inject(method = "updateScreen", at = @At("TAIL"))
    private void refreshCompletion(CallbackInfo ci) {
        kisdetweaks$refresh();
    }

    @Inject(method = "drawScreen(IIF)V", at = @At("TAIL"))
    private void drawSuggestions(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (!Configs.Generic.COMMAND_COMPLETION_ENABLED.getBooleanValue()) return;
        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        int width = resolution.getScaledWidth();
        int bottom = resolution.getScaledHeight() - 14;
        int first = kisdetweaks$completion.firstVisible();
        int rows = Math.min(8, kisdetweaks$completion.suggestions().size() - first);
        for (int row = 0; row < rows; row++) {
            int index = first + row;
            int y = bottom - (row + 1) * 12;
            int color = index == kisdetweaks$completion.selected() ? 0xE0606070 : 0xC0202020;
            GuiChat.drawRect(4, y, Math.min(width - 4, 224), y + 12, color);
            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(
                    kisdetweaks$completion.label(index), 8, y + 2, 0xFFFFFF);
        }
    }

    @Inject(method = "mouseClicked(III)V", at = @At("HEAD"), cancellable = true)
    private void clickSuggestion(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) throws IOException {
        if (!Configs.Generic.COMMAND_COMPLETION_ENABLED.getBooleanValue() || mouseButton != 0) return;
        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        int bottom = resolution.getScaledHeight() - 14;
        int first = kisdetweaks$completion.firstVisible();
        int rows = Math.min(8, kisdetweaks$completion.suggestions().size() - first);
        if (mouseY >= bottom || mouseY < bottom - rows * 12
                || mouseX < 4 || mouseX >= Math.min(resolution.getScaledWidth() - 4, 224)) return;
        int row = (bottom - 1 - mouseY) / 12;
        kisdetweaks$completion.select(first + row);
        kisdetweaks$apply(false);
        ci.cancel();
    }
}

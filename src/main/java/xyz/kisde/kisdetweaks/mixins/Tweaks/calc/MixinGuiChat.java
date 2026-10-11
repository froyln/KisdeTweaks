package xyz.kisde.kisdetweaks.mixins.Tweaks.calc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextComponentString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.kisde.kisdetweaks.util.Calculator;
import xyz.kisde.kisdetweaks.config.Configs;

@Mixin(GuiScreen.class)
public abstract class MixinGuiChat {
    @Inject(method = "sendChatMessage(Ljava/lang/String;Z)V", at = @At("HEAD"), cancellable = true)
    private void handleLocalCalc(String message, boolean addToChat, CallbackInfo ci) {
        if (!Configs.Generic.CALCULATOR_ENABLED.getBooleanValue()
                || (!message.equals("/calc") && !message.startsWith("/calc "))) return;
        ci.cancel();
        Minecraft mc = Minecraft.getMinecraft();
        String expression = message.length() > 5 ? message.substring(6).trim() : "";
        if (expression.isEmpty()) {
            mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString("Usage: /calc <expression>"));
            return;
        }
        try {
            String utilityResult = Calculator.command(expression);
            String value = utilityResult == null
                    ? java.math.BigDecimal.valueOf(Calculator.evaluate(expression)).stripTrailingZeros().toPlainString()
                    : utilityResult;
            GuiScreen.setClipboardString(value);
            mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(expression + " = " + value + " (copied)"));
        } catch (IllegalArgumentException e) {
            mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString("Invalid calculation: " + e.getMessage()));
        }
    }
}

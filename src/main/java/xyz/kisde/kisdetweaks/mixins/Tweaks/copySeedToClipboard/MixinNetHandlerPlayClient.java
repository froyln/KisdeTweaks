package xyz.kisde.kisdetweaks.mixins.Tweaks.copySeedToClipboard;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.kisde.kisdetweaks.config.TweaksToggle;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {
    @Inject(method = "handleChat", at = @At("TAIL"))
    private void copySeed(SPacketChat packet, CallbackInfo ci) {
        if (!TweaksToggle.COPY_SEED_TO_CLIPBOARD.getBooleanValue()
                || !(packet.getChatComponent() instanceof TextComponentTranslation)) return;

        TextComponentTranslation message = (TextComponentTranslation) packet.getChatComponent();
        Object[] arguments = message.getFormatArgs();
        // Server-side seed args are serialized to JSON as strings, so a Number check only works in singleplayer.
        if ("commands.seed.success".equals(message.getKey()) && arguments.length > 0
                && (arguments[0] instanceof Number || arguments[0] instanceof String)) {
            GuiScreen.setClipboardString(arguments[0].toString());
        }
    }
}

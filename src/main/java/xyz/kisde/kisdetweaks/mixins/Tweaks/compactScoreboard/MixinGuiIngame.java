package xyz.kisde.kisdetweaks.mixins.Tweaks.compactScoreboard;

import net.minecraft.client.gui.GuiIngame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.kisde.kisdetweaks.config.Hotkeys;
import xyz.kisde.kisdetweaks.config.TweaksToggle;
import xyz.kisde.kisdetweaks.util.CompactScoreboard;

@Mixin(GuiIngame.class)
public abstract class MixinGuiIngame {
    // Format both width measurement and drawing without changing authoritative scores.
    @Redirect(method = "renderScoreboard", at = @At(value = "INVOKE",
            target = "Ljava/lang/StringBuilder;append(I)Ljava/lang/StringBuilder;"), require = 2)
    private StringBuilder compactScore(StringBuilder builder, int score) {
        if (TweaksToggle.COMPACT_SCOREBOARD.getBooleanValue()
                && !Hotkeys.SHOW_FULL_SCOREBOARD_NUMBERS.getKeyBind().isKeyBindHeld()) {
            return builder.append(CompactScoreboard.format(score));
        }
        return builder.append(score);
    }
}

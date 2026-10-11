package xyz.kisde.kisdetweaks.mixins.Tweaks.soundController;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.kisde.kisdetweaks.config.Configs;

@Mixin(SoundManager.class)
public abstract class MixinSoundManager {
    @Inject(method = "getClampedVolume(Lnet/minecraft/client/audio/ISound;)F", at = @At("RETURN"), cancellable = true)
    private void kisdetweaks$applySoundGain(ISound sound, CallbackInfoReturnable<Float> cir) {
        if (!Configs.Generic.SOUND_CONTROLLER_ENABLED.getBooleanValue()) return;
        String id = sound.getSoundLocation().toString();
        for (String entry : Configs.Generic.SOUND_GAINS.getValue()) {
            int separator = entry.lastIndexOf('=');
            if (separator <= 0 || !entry.substring(0, separator).trim().equals(id)) continue;
            try {
                float gain = Float.parseFloat(entry.substring(separator + 1).trim());
                if (Float.isFinite(gain) && gain >= 0.0F && gain <= 1.0F) cir.setReturnValue(cir.getReturnValueF() * gain);
            } catch (NumberFormatException ignored) { }
            break;
        }
    }
}

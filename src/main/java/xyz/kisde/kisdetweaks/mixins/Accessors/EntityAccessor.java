package xyz.kisde.kisdetweaks.mixins.Accessors;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Invoker
    void invokeSetFlag(int flag, boolean set);

    @Invoker
    boolean invokeGetFlag(int flag);
}

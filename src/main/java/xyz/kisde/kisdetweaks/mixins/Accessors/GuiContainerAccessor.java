package xyz.kisde.kisdetweaks.mixins.Accessors;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiContainer.class)
public interface GuiContainerAccessor {
    @Invoker("checkHotbarKeys")
    boolean kisdetweaks$checkHotbarKeys(int keyCode);
    @Accessor("inventorySlots") Container kisdetweaks$getInventorySlots();
}

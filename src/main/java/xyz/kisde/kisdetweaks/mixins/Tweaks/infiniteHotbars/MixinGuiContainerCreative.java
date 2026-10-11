package xyz.kisde.kisdetweaks.mixins.Tweaks.infiniteHotbars;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.kisde.kisdetweaks.util.HotbarStore;
import xyz.kisde.kisdetweaks.mixins.Accessors.GuiContainerAccessor;
import xyz.kisde.kisdetweaks.config.Configs;

@Mixin(GuiContainerCreative.class)
public abstract class MixinGuiContainerCreative {
    @Shadow @Final private static InventoryBasic basicInventory;
    @Shadow private float currentScroll;
    @Shadow private void setCurrentCreativeTab(CreativeTabs tab) { }

    @Inject(method = "setCurrentCreativeTab(Lnet/minecraft/creativetab/CreativeTabs;)V", at = @At("TAIL"))
    private void kisdetweaks$appendSavedHotbars(CreativeTabs tab, CallbackInfo ci) {
        if (tab != CreativeTabs.HOTBAR || !Configs.Generic.INFINITE_SAVED_HOTBARS_ENABLED.getBooleanValue()) return;
        NonNullList<ItemStack> items = ((GuiContainerCreative.ContainerCreative) ((GuiContainerAccessor) this).kisdetweaks$getInventorySlots()).itemList;
        for (int i = 0; i < HotbarStore.size(); i++) {
            for (ItemStack stack : HotbarStore.get(i)) items.add(stack.copy());
        }
        ItemStack saveRow = new ItemStack(Items.PAPER);
        NBTTagCompound lock = saveRow.getOrCreateSubCompound("CustomCreativeLock");
        lock.setString("displayName", "Ctrl-click to save current hotbar");
        saveRow.setStackDisplayName("Ctrl-click to save current hotbar");
        items.add(saveRow);
        for (int i = 1; i < 9; i++) items.add(ItemStack.EMPTY);
        ((GuiContainerCreative.ContainerCreative) ((GuiContainerAccessor) this).kisdetweaks$getInventorySlots()).scrollTo(currentScroll);
    }

    @Inject(method = "handleMouseClick(Lnet/minecraft/inventory/Slot;IILnet/minecraft/inventory/ClickType;)V", at = @At("HEAD"), cancellable = true)
    private void kisdetweaks$handleSavedHotbarClick(Slot slot, int slotId, int mouseButton, ClickType type, CallbackInfo ci) {
        if (!Configs.Generic.INFINITE_SAVED_HOTBARS_ENABLED.getBooleanValue()) return;
        if (slot == null || slot.inventory != basicInventory || !GuiScreen.isCtrlKeyDown() && !(GuiScreen.isShiftKeyDown() && mouseButton == 1)) return;
        NonNullList<ItemStack> items = ((GuiContainerCreative.ContainerCreative) ((GuiContainerAccessor) this).kisdetweaks$getInventorySlots()).itemList;
        int maxShift = Math.max(0, (items.size() + 8) / 9 - 5);
        int row = slotId / 9 + (int) (currentScroll * maxShift + 0.5D);
        int customIndex = row - 9;
        int count = HotbarStore.size();
        if (customIndex < 0 || customIndex > count) return;
        if (GuiScreen.isCtrlKeyDown() && mouseButton == 0) {
            HotbarStore.saveCurrent(customIndex);
            ci.cancel();
            setCurrentCreativeTab(CreativeTabs.HOTBAR);
        } else if (GuiScreen.isShiftKeyDown() && mouseButton == 1 && customIndex < count) {
            HotbarStore.loadToPlayer(customIndex);
            ci.cancel();
        } else if (GuiScreen.isCtrlKeyDown() && mouseButton == 1 && customIndex < count) {
            HotbarStore.delete(customIndex);
            ci.cancel();
            setCurrentCreativeTab(CreativeTabs.HOTBAR);
        }
    }
}

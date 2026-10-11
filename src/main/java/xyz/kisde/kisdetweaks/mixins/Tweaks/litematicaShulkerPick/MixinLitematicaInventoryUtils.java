package xyz.kisde.kisdetweaks.mixins.Tweaks.litematicaShulkerPick;

import net.minecraft.block.BlockShulkerBox;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.EnumHand;
import net.minecraft.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.kisde.kisdetweaks.config.Configs;

@Pseudo
@Mixin(targets = "fi.dy.masa.litematica.util.InventoryUtils")
public abstract class MixinLitematicaInventoryUtils {
    @Inject(method = "doPickBlockForStack", at = @At("RETURN"), cancellable = true, remap = false)
    private static void kisdetweaks$pickShulker(ItemStack requested, Minecraft mc, CallbackInfoReturnable<EnumHand> cir) {
        if (!Configs.Generic.LITEMATICA_SHULKER_PICK_ENABLED.getBooleanValue()
                || cir.getReturnValue() != null || requested == null || requested.isEmpty() || mc.player == null
                || mc.playerController == null || mc.player.openContainer != mc.player.inventoryContainer
                || !mc.player.inventory.getItemStack().isEmpty()) return;
        NonNullList<ItemStack> inventory = mc.player.inventory.mainInventory;
        for (int source = 0; source < inventory.size(); source++) {
            ItemStack box = inventory.get(source);
            if (!isBox(box) || !contains(box, requested)) continue;
            int hotbar = mc.player.inventory.currentItem;
            if (source < 9) {
                mc.player.inventory.currentItem = source;
                mc.playerController.updateController();
            } else {
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId,
                        source, hotbar, ClickType.SWAP, mc.player);
            }
            cir.setReturnValue(EnumHand.MAIN_HAND);
            return;
        }
    }

    private static boolean isBox(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemBlock
                && ((ItemBlock) stack.getItem()).getBlock() instanceof BlockShulkerBox;
    }

    private static boolean contains(ItemStack box, ItemStack wanted) {
        NBTTagCompound blockEntity = box.getSubCompound("BlockEntityTag");
        if (blockEntity == null) return false;
        NBTTagList items = blockEntity.getTagList("Items", 10);
        for (int i = 0; i < items.tagCount(); i++) {
            ItemStack stored = new ItemStack(items.getCompoundTagAt(i));
            if (ItemStack.areItemsEqual(stored, wanted)
                    && (wanted.getTagCompound() == null || ItemStack.areItemStackTagsEqual(stored, wanted))) return true;
        }
        return false;
    }
}

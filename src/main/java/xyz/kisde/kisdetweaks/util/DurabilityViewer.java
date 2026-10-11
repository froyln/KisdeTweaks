package xyz.kisde.kisdetweaks.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import xyz.kisde.kisdetweaks.config.TweaksToggle;

import java.util.ArrayList;
import java.util.List;

public final class DurabilityViewer {
    private DurabilityViewer() { }

    public static void render() {
        Minecraft mc = Minecraft.getMinecraft();
        if (!TweaksToggle.DURABILITY_VIEWER.getBooleanValue() || mc.player == null || mc.world == null || mc.currentScreen != null) return;

        List<ItemStack> items = new ArrayList<>();
        items.add(mc.player.getHeldItemMainhand());
        items.add(mc.player.getHeldItemOffhand());
        items.addAll(mc.player.inventory.armorInventory);

        ScaledResolution resolution = new ScaledResolution(mc);
        int x = resolution.getScaledWidth() - 6;
        int y = resolution.getScaledHeight() - 24;
        for (ItemStack stack : items) {
            if (stack.isEmpty() || !stack.isItemStackDamageable()) continue;

            int maxDamage = stack.getMaxDamage();
            int damage = stack.getItemDamage();
            int remaining = Math.max(0, maxDamage - damage);
            int percent = maxDamage == 0 ? 100 : remaining * 100 / maxDamage;
            String value = percent >= 90 ? "-" + damage : Integer.toString(remaining);
            int color = percent <= 10 ? 0xFFFF5555 : percent <= 25 ? 0xFFFFAA00 : percent <= 75 ? 0xFFFFFFFF : 0xFF55FF55;

            y -= 18;
            mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x - 16, y);
            mc.fontRenderer.drawStringWithShadow(value, x - 18 - mc.fontRenderer.getStringWidth(value), y + 4, color);
        }

        int emptySlots = 0;
        NonNullList<ItemStack> inventory = mc.player.inventory.mainInventory;
        for (ItemStack stack : inventory) if (stack.isEmpty()) emptySlots++;
        String slots = "Empty: " + emptySlots;
        mc.fontRenderer.drawStringWithShadow(slots, x - mc.fontRenderer.getStringWidth(slots), y - 8, 0xFFFFFFFF);
    }
}

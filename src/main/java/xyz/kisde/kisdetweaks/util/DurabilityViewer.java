package xyz.kisde.kisdetweaks.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import xyz.kisde.kisdetweaks.config.Configs;
import xyz.kisde.kisdetweaks.config.TweaksToggle;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class DurabilityViewer {
    private static final ItemStack[] LAST_STACKS = new ItemStack[6];
    private static final int[] LAST_REMAINING = new int[6];
    private static World lastWorld;
    private static ItemStack warningStack = ItemStack.EMPTY;
    private static long warningTime;

    private DurabilityViewer() { }

    public static void render() {
        Minecraft mc = Minecraft.getMinecraft();
        if (!TweaksToggle.DURABILITY_VIEWER.getBooleanValue() || mc.player == null || mc.world == null || mc.currentScreen != null) return;
        if (lastWorld != mc.world) {
            lastWorld = mc.world;
            for (int i = 0; i < LAST_STACKS.length; i++) LAST_STACKS[i] = ItemStack.EMPTY;
        }

        List<ItemStack> items = new ArrayList<>();
        items.add(mc.player.getHeldItemMainhand());
        items.add(mc.player.getHeldItemOffhand());
        items.addAll(mc.player.inventory.armorInventory);
        int warningIndex = 0;
        boolean playWarning = false;
        for (ItemStack stack : items) {
            if (!stack.isEmpty() && stack.isItemStackDamageable()) playWarning |= updateWarning(stack, warningIndex);
            else LAST_STACKS[warningIndex] = ItemStack.EMPTY;
            warningIndex++;
        }
        for (; warningIndex < LAST_STACKS.length; warningIndex++) LAST_STACKS[warningIndex] = ItemStack.EMPTY;
        String warningMode = Configs.Generic.DURABILITY_WARNING_MODE.getValue().getName();
        if (playWarning) {
            if ("sound".equals(warningMode) || "both".equals(warningMode)) mc.player.playSound(SoundEvents.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
            if ("visual".equals(warningMode) || "both".equals(warningMode)) {
                warningStack = findWarningItem(items);
                warningTime = System.currentTimeMillis();
            }
        }

        ScaledResolution resolution = new ScaledResolution(mc);
        drawHud(mc);
        if (!warningStack.isEmpty() && System.currentTimeMillis() - warningTime < 1200L) renderWarning(mc, resolution);
        else warningStack = ItemStack.EMPTY;
    }

    public static int[] renderPreview() {
        return drawHud(Minecraft.getMinecraft());
    }

    private static int[] drawHud(Minecraft mc) {
        List<ItemStack> items = new ArrayList<>();
        if (mc.player != null) {
            items.add(mc.player.getHeldItemMainhand());
            items.add(mc.player.getHeldItemOffhand());
            items.addAll(mc.player.inventory.armorInventory);
        }
        ScaledResolution resolution = new ScaledResolution(mc);
        String corner = Configs.Generic.DURABILITY_HUD_CORNER.getValue().getName();
        boolean left = corner.endsWith("left");
        boolean top = corner.startsWith("top");
        int xOffset = Configs.Generic.DURABILITY_HUD_X_OFFSET.getIntegerValue();
        int yOffset = Configs.Generic.DURABILITY_HUD_Y_OFFSET.getIntegerValue();
        int edgeX = left ? 6 + xOffset : resolution.getScaledWidth() - 6 - xOffset;
        int y = top ? 6 + yOffset : resolution.getScaledHeight() - 24 - yOffset;
        if (top) y += 18;
        int[] bounds = {edgeX, y, edgeX, y};
        RenderHelper.enableGUIStandardItemLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        for (ItemStack stack : items) {
            if (stack.isEmpty() || !stack.isItemStackDamageable()) continue;
            int maxDamage = stack.getMaxDamage();
            int damage = stack.getItemDamage();
            int remaining = Math.max(0, maxDamage - damage);
            int percent = maxDamage == 0 ? 100 : remaining * 100 / maxDamage;
            if (percent > Configs.Generic.DURABILITY_HIDE_OVER_PERCENT.getIntegerValue()) continue;
            String value;
            if (Configs.Generic.DURABILITY_PERCENTAGES.getBooleanValue()) value = String.format(Locale.ROOT, "%.1f%%", remaining * 100.0 / maxDamage);
            else if (percent >= Configs.Generic.DURABILITY_SHOW_DAMAGE_AT_PERCENT.getIntegerValue()) value = "-" + damage;
            else value = Integer.toString(remaining);
            int color = percent <= 10 ? 0xFFFF5555 : percent <= 25 ? 0xFFFFAA00 : percent <= 75 ? 0xFFFFFFFF : 0xFF55FF55;
            y += top ? 18 : -18;
            int iconX = left ? edgeX : edgeX - 16;
            mc.getRenderItem().renderItemAndEffectIntoGUI(stack, iconX, y);
            int textX = left ? edgeX + 18 : edgeX - 18 - mc.fontRenderer.getStringWidth(value);
            mc.fontRenderer.drawStringWithShadow(value, textX, y + 4, color);
            includeRow(bounds, iconX, textX, mc.fontRenderer.getStringWidth(value), y);
        }

        if (Configs.Generic.DURABILITY_SHOW_EMPTY.getBooleanValue()) {
            int emptySlots = 0;
            if (mc.player != null) {
                NonNullList<ItemStack> inventory = mc.player.inventory.mainInventory;
                for (ItemStack stack : inventory) if (stack.isEmpty()) emptySlots++;
            }
            y += top ? 18 : -18;
            int iconX = left ? edgeX : edgeX - 16;
            mc.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Blocks.CHEST), iconX, y);
            String slots = Integer.toString(emptySlots);
            int textX = left ? edgeX + 18 : edgeX - 18 - mc.fontRenderer.getStringWidth(slots);
            mc.fontRenderer.drawStringWithShadow(slots, textX, y + 4, 0xFFFFFFFF);
            includeRow(bounds, iconX, textX, mc.fontRenderer.getStringWidth(slots), y);
        }
        RenderHelper.disableStandardItemLighting();
        return bounds;
    }

    private static void includeRow(int[] bounds, int iconX, int textX, int textWidth, int y) {
        bounds[0] = Math.min(bounds[0], Math.min(iconX, textX));
        bounds[1] = Math.min(bounds[1], y);
        bounds[2] = Math.max(bounds[2], Math.max(iconX + 16, textX + textWidth));
        bounds[3] = Math.max(bounds[3], y + 16);
    }

    private static boolean updateWarning(ItemStack stack, int slot) {
        int remaining = Math.max(0, stack.getMaxDamage() - stack.getItemDamage());
        ItemStack previous = LAST_STACKS[slot];
        boolean sameItem = !previous.isEmpty() && previous.getItem() == stack.getItem()
                && ItemStack.areItemStackTagsEqual(previous, stack);
        boolean crossed = sameItem && remaining < LAST_REMAINING[slot]
                && remaining <= Configs.Generic.DURABILITY_WARNING_AMOUNT.getIntegerValue()
                && remaining * 100 / stack.getMaxDamage() <= Configs.Generic.DURABILITY_WARNING_PERCENT.getIntegerValue();
        LAST_STACKS[slot] = stack.copy();
        LAST_REMAINING[slot] = remaining;
        return crossed;
    }

    private static ItemStack findWarningItem(List<ItemStack> items) {
        for (ItemStack stack : items) {
            if (!stack.isEmpty() && stack.isItemStackDamageable()
                    && stack.getMaxDamage() - stack.getItemDamage() <= Configs.Generic.DURABILITY_WARNING_AMOUNT.getIntegerValue()
                    && (stack.getMaxDamage() - stack.getItemDamage()) * 100 / stack.getMaxDamage() <= Configs.Generic.DURABILITY_WARNING_PERCENT.getIntegerValue()) return stack.copy();
        }
        return ItemStack.EMPTY;
    }

    private static void renderWarning(Minecraft mc, ScaledResolution resolution) {
        String label = net.minecraft.client.resources.I18n.format("kisdetweaks.message.low_durability");
        int x = (resolution.getScaledWidth() - 110) / 2;
        int y = resolution.getScaledHeight() / 2 - 20;
        Gui.drawRect(x, y, x + 110, y + 32, 0xC0000000);
        mc.getRenderItem().renderItemAndEffectIntoGUI(warningStack, x + 5, y + 7);
        mc.fontRenderer.drawStringWithShadow(label, x + 26, y + 11, 0xFFFF5555);
    }
}

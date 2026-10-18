package xyz.kisde.kisdetweaks.mixins.Fixes;

import net.minecraft.client.Minecraft;
import fi.dy.masa.malilib.input.InputDispatcherImpl;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.kisde.kisdetweaks.config.Configs;
import xyz.kisde.kisdetweaks.mixins.Accessors.GuiContainerAccessor;

// Run before MaLiLib dispatches conflicting inventory hotkeys to other mods.
@Mixin(value = InputDispatcherImpl.class, remap = false)
public abstract class MixinInputDispatcher {
    @Inject(method = "onKeyInput", at = @At("HEAD"), cancellable = true)
    private void kisdetweaks$preferHotbarKey(CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getMinecraft();
        if (Keyboard.getEventKeyState() && !Keyboard.isRepeatEvent() && kisdetweaks$inventoryKey(mc, Keyboard.getEventKey())) {
            cir.setReturnValue(true);
        }
    }
    @Inject(method = "onMouseInput", at = @At("HEAD"), cancellable = true)
    private void kisdetweaks$preferHotbarMouse(CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getMinecraft();
        int button = Mouse.getEventButton();
        if (button >= 0 && Mouse.getEventButtonState() && kisdetweaks$inventoryKey(mc, button - 100)) {
            cir.setReturnValue(true);
        }
    }

    private static boolean kisdetweaks$inventoryKey(Minecraft mc, int key) {
        if (!(mc.currentScreen instanceof GuiContainer)) return false;
        if (Configs.Fixes.HOTBAR_HOTKEYS_FIX.getBooleanValue() && ((GuiContainerAccessor) mc.currentScreen).kisdetweaks$checkHotbarKeys(key)) return true;
        if (!Configs.Fixes.INVENTORY_OFFHAND_SWAP_FIX.getBooleanValue() || key == 0
                || key != mc.gameSettings.keyBindSwapHands.getKeyCode()
                || !(mc.currentScreen instanceof GuiInventory) || mc.playerController == null
                || mc.player.openContainer != mc.player.inventoryContainer
                || !mc.player.inventory.getItemStack().isEmpty()) return false;
        Slot source = ((GuiContainerAccessor) mc.currentScreen).kisdetweaks$getHoveredSlot();
        Slot offhand = mc.player.inventoryContainer.getSlot(45);
        if (source == null || source == offhand || source.inventory != mc.player.inventory || !source.canTakeStack(mc.player)
                || !offhand.canTakeStack(mc.player)
                || (!offhand.getStack().isEmpty() && !source.isItemValid(offhand.getStack()))
                || (!source.getStack().isEmpty() && (!offhand.isItemValid(source.getStack())
                    || source.getStack().getCount() > offhand.getItemStackLimit(source.getStack())))
                || (!offhand.getStack().isEmpty()
                    && offhand.getStack().getCount() > source.getItemStackLimit(offhand.getStack()))) return false;
        int window = mc.player.inventoryContainer.windowId;
        mc.playerController.windowClick(window, source.slotNumber, 0, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(window, 45, 0, ClickType.PICKUP, mc.player);
        if (!mc.player.inventory.getItemStack().isEmpty()) {
            mc.playerController.windowClick(window, source.slotNumber, 0, ClickType.PICKUP, mc.player);
        }
        return true;
    }
}

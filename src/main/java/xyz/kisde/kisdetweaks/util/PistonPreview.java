package xyz.kisde.kisdetweaks.util;

import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.BlockPistonStructureHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import xyz.kisde.kisdetweaks.config.Configs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class PistonPreview {
    private static final List<Display> DISPLAYS = new ArrayList<>();

    private PistonPreview() { }

    public static boolean handleRightClick(WorldClient world, BlockPos pos, EntityPlayer player) {
        if (!Configs.Generic.PISTORDER_ENABLED.getBooleanValue() || player.isSneaking() || !player.getHeldItemMainhand().isEmpty()) return false;
        IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockPistonBase)) return false;
        boolean sticky = state.getBlock() == Blocks.STICKY_PISTON;
        boolean extended = state.getValue(BlockPistonBase.EXTENDED);
        if (extended && !sticky) return false;

        Display display = find(world, pos);
        if (display == null) {
            DISPLAYS.add(new Display(world, pos, state));
        } else if (!display.indirect && display.canUseIndirectMode()) {
            display.indirect = true;
            display.refresh();
        } else {
            DISPLAYS.remove(display);
        }
        return true;
    }

    public static void clear() { DISPLAYS.clear(); }

    public static void render(float partialTicks) {
        if (!Configs.Generic.PISTORDER_ENABLED.getBooleanValue()) { clear(); return; }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.player == null) { clear(); return; }
        RenderManager renderer = mc.getRenderManager();
        Iterator<Display> iterator = DISPLAYS.iterator();
        while (iterator.hasNext()) {
            Display display = iterator.next();
            if (display.world != mc.world || !display.world.getBlockState(display.pos).equals(display.state)) {
                iterator.remove();
                continue;
            }
            display.refresh();
            display.draw(mc, renderer, partialTicks);
        }
    }

    private static Display find(WorldClient world, BlockPos pos) {
        for (Display display : DISPLAYS) if (display.world == world && display.pos.equals(pos)) return display;
        return null;
    }

    private static final class Display {
        private final WorldClient world;
        private final BlockPos pos;
        private final IBlockState state;
        private final EnumFacing facing;
        private final boolean sticky;
        private final boolean extending;
        private final List<BlockPos> moved = new ArrayList<>();
        private final List<BlockPos> destroyed = new ArrayList<>();
        private boolean indirect;
        private boolean canMove;
        private BlockPos immovable;

        private Display(WorldClient world, BlockPos pos, IBlockState state) {
            this.world = world;
            this.pos = pos.toImmutable();
            this.state = state;
            this.facing = state.getValue(BlockPistonBase.FACING);
            this.sticky = state.getBlock() == Blocks.STICKY_PISTON;
            this.extending = !state.getValue(BlockPistonBase.EXTENDED);
            refresh();
        }

        private boolean canUseIndirectMode() {
            return world.isAirBlock(pos.offset(facing)) && !world.isAirBlock(pos.offset(facing, 2));
        }

        private void refresh() {
            BlockPos simulatedPiston = pos;
            boolean simulatedExtending = extending;
            if (indirect && !sticky) {
                simulatedPiston = pos.offset(facing);
                simulatedExtending = true;
            }
            BlockPistonStructureHelper helper = new BlockPistonStructureHelper(world, simulatedPiston, facing, simulatedExtending);
            canMove = helper.canMove();
            moved.clear();
            moved.addAll(helper.getBlocksToMove());
            Collections.reverse(moved);
            destroyed.clear();
            destroyed.addAll(helper.getBlocksToDestroy());
            Collections.reverse(destroyed);
            immovable = null;
            if (!canMove && moved.size() < 12) {
                EnumFacing moveDirection = simulatedExtending ? facing : facing.getOpposite();
                BlockPos candidate = simulatedExtending
                        ? simulatedPiston.offset(facing, moved.size() + 1)
                        : simulatedPiston.offset(facing, 2).offset(moveDirection, moved.size());
                IBlockState candidateState = world.getBlockState(candidate);
                if (!candidateState.getMaterial().isReplaceable()
                        && candidateState.getPushReaction() != EnumPushReaction.DESTROY
                        && !BlockPistonBase.canPush(candidateState, world, candidate, moveDirection, false, moveDirection)) {
                    immovable = candidate;
                }
            }
        }

        private void draw(Minecraft mc, RenderManager renderer, float partialTicks) {
            if (mc.player.getDistanceSq(pos) > 64.0D * 64.0D) return;
            String mode = indirect ? " (Indirect)" : "";
            String action = (extending ? "Push" : "Retract") + mode + " "
                    + (canMove ? TextFormatting.GREEN + "√" : TextFormatting.RED + "×");
            drawLabel(mc, renderer, pos, 0.55D, action, partialTicks);
            drawLabel(mc, renderer, pos, 0.35D, TextFormatting.GOLD + "Blocks: " + TextFormatting.WHITE + moved.size(), partialTicks);
            for (int i = 0; i < moved.size(); i++) {
                drawLabel(mc, renderer, moved.get(i), 0.5D, TextFormatting.AQUA.toString() + (i + 1), partialTicks);
            }
            for (int i = 0; i < destroyed.size(); i++) {
                drawLabel(mc, renderer, destroyed.get(i), 0.5D, TextFormatting.RED.toString() + (i + 1), partialTicks);
            }
            if (immovable != null) drawLabel(mc, renderer, immovable, 0.5D, TextFormatting.DARK_RED + "×", partialTicks);
        }
    }

    private static void drawLabel(Minecraft mc, RenderManager renderer, BlockPos pos, double yOffset, String text, float partialTicks) {
        double x = pos.getX() + 0.5D - renderer.viewerPosX;
        double y = pos.getY() + yOffset - renderer.viewerPosY;
        double z = pos.getZ() + 0.5D - renderer.viewerPosZ;
        if (x * x + y * y + z * z > 64.0D * 64.0D) return;
        EntityRenderer.drawNameplate(mc.fontRenderer, text, (float)x, (float)y, (float)z, 0,
                renderer.playerViewY, renderer.playerViewX, mc.gameSettings.thirdPersonView == 2, false);
    }
}

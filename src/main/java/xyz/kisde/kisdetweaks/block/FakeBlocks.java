package xyz.kisde.kisdetweaks.block;

import net.minecraft.block.Block;
import net.minecraft.init.Bootstrap;
import xyz.kisde.kisdetweaks.mixins.Accessors.BlocksAccessor;

public class FakeBlocks {
    public static final Block FAKE_BARRIER;
    public static final Block FAKE_STRUCTURE_VOID;

    static {
        if (!Bootstrap.isRegistered()) {
            throw new RuntimeException("Accessed Blocks before Bootstrap! but why?");
        } else {
            FAKE_BARRIER = BlocksAccessor.invokeGetRegisteredBlock("fakebarrier");
            FAKE_STRUCTURE_VOID = BlocksAccessor.invokeGetRegisteredBlock("fakesv");
        }
    }
}

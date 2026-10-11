package xyz.kisde.kisdetweaks.util;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.StandardCopyOption;
import java.util.List;

public final class HotbarStore {
    private static final List<NonNullList<ItemStack>> ROWS = Lists.newArrayList();
    private static boolean loaded;
    private static boolean failed;

    private HotbarStore() { }

    private static File file() {
        return new File(Minecraft.getMinecraft().gameDir, "config/kisdetweaks-hotbars.dat");
    }

    private static void load() {
        if (loaded) return;
        loaded = true;
        File file = file();
        if (!file.isFile()) return;
        try {
            NBTTagList rows = CompressedStreamTools.read(file).getTagList("Rows", 10);
            if (rows.tagCount() > 10000) throw new IllegalStateException("saved hotbar file exceeds 10000 rows");
            for (int i = 0; i < rows.tagCount(); i++) {
                NBTTagList items = rows.getCompoundTagAt(i).getTagList("Items", 10);
                NonNullList<ItemStack> row = NonNullList.withSize(9, ItemStack.EMPTY);
                for (int j = 0; j < Math.min(9, items.tagCount()); j++) row.set(j, new ItemStack(items.getCompoundTagAt(j)));
                ROWS.add(row);
            }
        } catch (Exception e) {
            failed = true;
            System.err.println("KisdeTweaks could not read saved hotbars; preserving the file: " + e.getMessage());
        }
    }

    public static int size() { load(); return ROWS.size(); }

    public static NonNullList<ItemStack> get(int index) { load(); return ROWS.get(index); }

    public static void saveCurrent(int index) {
        load();
        if (failed) return;
        NonNullList<ItemStack> row = NonNullList.withSize(9, ItemStack.EMPTY);
        for (int i = 0; i < 9; i++) row.set(i, Minecraft.getMinecraft().player.inventory.getStackInSlot(i).copy());
        if (index == ROWS.size()) ROWS.add(row); else ROWS.set(index, row);
        write();
    }

    public static void loadToPlayer(int index) {
        load();
        NonNullList<ItemStack> row = ROWS.get(index);
        Minecraft mc = Minecraft.getMinecraft();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = row.get(i).copy();
            mc.player.inventory.setInventorySlotContents(i, stack);
            mc.playerController.sendSlotPacket(stack, 36 + i);
        }
        mc.player.inventoryContainer.detectAndSendChanges();
    }

    public static void delete(int index) {
        load();
        if (failed) return;
        ROWS.remove(index);
        write();
    }

    private static void write() {
        try {
            NBTTagList rows = new NBTTagList();
            for (NonNullList<ItemStack> row : ROWS) {
                NBTTagCompound rowTag = new NBTTagCompound();
                NBTTagList items = new NBTTagList();
                for (ItemStack stack : row) items.appendTag(stack.writeToNBT(new NBTTagCompound()));
                rowTag.setTag("Items", items);
                rows.appendTag(rowTag);
            }
            NBTTagCompound root = new NBTTagCompound();
            root.setInteger("Version", 1);
            root.setTag("Rows", rows);
            File file = file();
            File parent = file.getParentFile();
            if (!parent.isDirectory() && !parent.mkdirs()) throw new IllegalStateException("cannot create config directory");
            File temp = new File(parent, file.getName() + ".tmp");
            CompressedStreamTools.write(root, temp);
            if (file.isFile()) Files.copy(file.toPath(), new File(parent, file.getName() + ".bak").toPath(), StandardCopyOption.REPLACE_EXISTING);
            try {
                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            System.err.println("KisdeTweaks could not save hotbars: " + e.getMessage());
        }
    }
}

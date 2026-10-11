# Compatibility

- Minecraft Java Edition 1.12.2.
- LiteLoader `1.12.2-SNAPSHOT`.
- Required MaLiLib `0.52.0`, LiteLoader 1.12.2 build.
- Optional Litematica shulker-pick integration; the tested instance uses Litematica 0.31.4 and MaLiLib 0.54.0.
- Forge and OptiFine combinations have not been validated.

The project derives from LiteTweaker commit `8989bf167491ea9464b49e1341ade807de946893` under LGPL-3.0. The mod id, Java packages, metadata, and asset namespace use `kisdetweaks` / `xyz.kisde.kisdetweaks`.

Focused Minecraft runtime checks cover inventory hotkeys, redstone palette reloads, book editing, and Fake Night Vision with VoxelMap 1.7.1. A complete feature audit and multiplayer validation remain outstanding.

## Inventory regression checks

In a disposable singleplayer world with Litematica 0.31.4:

1. Put a shulker containing the schematic's requested item in main inventory, remove loose copies of that item, switch to survival, and use Litematica pick-block. The box must reach the selected hotbar slot without a crash; after reopening inventory or rejoining, both the box NBT and displaced item must be preserved.
2. Repeat with a matching box already in the hotbar: select that slot without moving its contents. With no matching box or the toggle disabled, leave Litematica's ordinary result unchanged.
3. Bind F to hotbar slot 7, hover an inventory item, and press F with **Fixes → Hotbar Hotkeys Fix** enabled. Only slot 7 should swap; offhand must remain unchanged. Disable the option to restore the other mods' handling. With an item held by the cursor, leave the usual input handling unchanged.
4. Bind a mouse side button to hotbar slot 8 and repeat the hovered-item swap; confirm the offhand remains unchanged and both stacks persist on the server. Wheel events must not count as button presses.
5. Enable the XeKr Blue resource pack and OptiFine Redstone Colors. Check powered and unpowered dust, disable the option to restore red tint, then enable it and reload resources with F3+T. The dust must return to blue/cyan.

## Book editing and night vision regression checks

- Run `bash scripts/test-book-editor.sh` for selection, replacement, rejected edits, Unicode deletion, wrapped lines, and vertical navigation.
- In an unsigned book, use arrows and Shift+Arrows across wrapped lines; copy, cut, paste, delete, and replace a selection. Close and reopen to confirm saved text. Page changes must reset selection; signing and disabling Better Books must keep vanilla behavior.
- With VoxelMap enabled and no night vision potion, toggle Fake Night Vision repeatedly and check both the HUD and lighting. A real night vision effect must still expire normally; fake vision must never appear in the player potion inventory.

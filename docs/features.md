# Feature status

## Implemented in source

- **Inherited LiteTweaker feature set:** source retained from the pinned LiteTweaker snapshot. Full per-feature runtime audit remains outstanding.
- **Safe portal placement:** enabled by default; a MaLiLib option and hotkey toggle prevents `ItemBlock` placements in the portal plane beside Nether Portal blocks. Placements in front and behind are allowed. Non-`ItemBlock` placement paths remain unaudited.
- **Entity render disabler:** opt-in global toggle plus a registry-backed entity type list suppresses selected entities at `RenderManager.renderEntityStatic`. Entity simulation and interaction continue.
- **Local `/calc`:** evaluates bounded arithmetic expressions locally, so the command is never sent to a server. Supports decimals, parentheses, unary signs, `+`, `-`, `*`, `/`, right-associative `^`, CalcMod's listed stack/shulker/time and player variables, and the `rates`, `itemtosb`, `sbtoitem`, stack/item, portal-coordinate, and distance utilities. Results copy to the clipboard. Crafting, storage, custom functions, comparator, bartering, and hopper-timing commands remain unimplemented.
- **Per-sound volume:** a searchable menu lists registered sounds and lets you mute or reduce each sound individually. Saved `soundGains` rules preserve the volume settings. Boosts above 100%, preview playback, presets, and regions are unavailable.
- **Infinite Creative hotbars:** the Creative Hotbar tab appends expandable rows stored in `config/kisdetweaks-hotbars.dat`, preserving item NBT. Ctrl-left saves/overwrites, Shift-right loads, and Ctrl-right deletes. A damaged store is left untouched and disables writes.
- **Pistorder preview:** right-click a piston with an empty main hand to show movement order and destroyed blocks with labels in the world. Use the clear hotkey to remove displays. The preview never changes server world state.
- **OptiFine redstone colors:** the Fixes tab enables a resource pack's 16×1 redstone palette (`optifine/colormap/redstone.png` or legacy `mcpatcher/colormap/redstone.png`) for wire tint and particles. This includes XeKr Blue redstone; it does not implement other OptiFine texture features. Toggle changes rebuild rendered chunks, and resource reloads refresh the palette.
- **Hotbar hotkeys fix:** the Fixes tab lets a hotbar key or mouse button take priority over overlapping mod inventory hotkeys when hovering an inventory slot. Disable to restore other mods' input handling.
- **Litematica shulker pick:** if Litematica's ordinary requested-stack pick fails, KisdeTweaks searches carried shulker boxes and selects an existing hotbar box or swaps an inventory box into the selected hotbar slot using a normal server-validated inventory click. Litematica remains optional.

- **Better Books:** adds a visible caret, arrow navigation, Shift+Arrow selection, Ctrl+Left/Right word navigation, Home/End, Delete, and selection-aware copy/cut/paste. Click text to place the caret; Shift+Click extends selection. Editing keeps vanilla page limits and signing behavior.
- **Command completion:** command suggestions open automatically while typing a command. Arrow keys navigate suggestions; history navigation remains available outside suggestion selection.
- **Fake Night Vision:** rendering-only brightness avoids advertising nonexistent potion effects to VoxelMap and other potion consumers.

Every standalone feature above has an enable toggle in the KisdeTweaks MaLiLib config menu. The inherited LiteTweaker features retain their existing individual toggles.

## Partial; remaining work

- Chat suggestion cursor alignment and advanced scrolling need further validation.
- CalcMod's remaining utilities
- Sound presets, preview playback, and regions

Source compilation does not prove runtime Mixin compatibility; features are not fully verified until checked in Minecraft 1.12.2.

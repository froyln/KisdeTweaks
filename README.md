<div align="center">

# KisdeTweaks

**A client-side collection of configurable gameplay and interface tweaks for Minecraft Java 1.12.2 on LiteLoader.**

</div>

---

## What is it?

KisdeTweaks extends the LiteTweaker 1.12.2 project and uses MaLiLib for configuration and hotkeys. It is under active development; features are being ported and added in stages.

## Documentation

**[Project documentation](docs/README.md)**

## Features

- Inherited LiteTweaker 1.12.2 tweaks.
- Optional protection against placing blocks beside Nether Portal sides.
- Optional entity render suppression by entity type.
- Local arithmetic `/calc` command and automatic command suggestions.
- Expandable saved hotbars and optional Litematica shulker picking.
- Piston previews, searchable sound volume controls, and enhanced book editing.
- Inventory hotkey fixes and resource-pack redstone colors.

## Requirements

- Minecraft Java Edition 1.12.2
- LiteLoader for 1.12.2
- MaLiLib compatible with the LiteLoader 1.12.2 API

## Installation

1. Download the `.litemod` file from the [latest release](https://github.com/froyln/KisdeTweaks/releases/latest).
2. Install LiteLoader for Minecraft 1.12.2 and a compatible MaLiLib version.
3. Put KisdeTweaks and MaLiLib in your instance’s `minecraft/mods/1.12.2/` folder. Replace older KisdeTweaks files to avoid duplicates.
4. Restart Minecraft. Press **End** to open the configuration menu.

## Building from Source

Use a JDK compatible with the legacy Minecraft build tooling, then run `bash gradlew clean build`. LiteLoader artifacts are generated under `build/libs/`.

## License

KisdeTweaks is based on LiteTweaker, licensed under GNU LGPL version 3. See [LICENSE](LICENSE) and preserve the upstream copyright notices.

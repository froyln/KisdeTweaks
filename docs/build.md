# Building

The project uses Minecraft 1.12.2, MCP `stable_39`, LiteLoader `1.12.2-SNAPSHOT`, MaLiLib `0.52.0`, Gradle 4.10.3, ForgeGradle 2.3, and Mixin Gradle 0.4. The matching LiteLoader and MaLiLib jars are in `libs/`; the compiled mod depends on MaLiLib rather than embedding it.

```sh
bash gradlew clean build
```

The build succeeds on OpenJDK 21.0.12 in this environment. ForgeGradle warns that this toolchain is unsupported and javac warns about Java 8 source/target options. Use Java 8 to reproduce the original target runtime where available.

The distributable artifact is `build/libs/mod-kisdetweaks-0.1.0-mc1.12.2.litemod`; it includes the generated `mixins.kisdetweaks.refmap.json` required by obfuscated Minecraft runtime names. The separate `-sources.jar` is for development.

package xyz.kisde.kisdetweaks.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import xyz.kisde.kisdetweaks.config.Configs;

public final class RedstoneColors implements IResourceManagerReloadListener {
    private static int[] colors;

    @Override
    public void onResourceManagerReload(IResourceManager manager) {
        colors = null;
        for (String folder : new String[]{"optifine", "mcpatcher"}) {
            ResourceLocation location = new ResourceLocation("minecraft", folder + "/colormap/redstone.png");
            try (IResource resource = manager.getResource(location)) {
                BufferedImage image = ImageIO.read(resource.getInputStream());
                if (image != null && image.getWidth() == 16 && image.getHeight() == 1) {
                    colors = image.getRGB(0, 0, 16, 1, null, 0, 16);
                    return;
                }
            } catch (IOException ignored) {
                // Packs without this optional palette use vanilla colors.
            }
        }
    }

    public static int getColor(int power) {
        return Configs.Fixes.OPTIFINE_REDSTONE_COLORS.getBooleanValue() && colors != null
                && power >= 0 && power < 16 ? colors[power] & 0xFFFFFF : -1;
    }
}

package msifeed.mc.gui.render;

import net.minecraft.util.ResourceLocation;

public class TextureInfo {
    public final ResourceLocation resource;
    public final int u, v;
    public final int width, height;

    public TextureInfo(String res, int u, int v) {
        this(res, u, v, 0, 0);
    }

    public TextureInfo(String resLoc, int u, int v, int width, int height) {
        this(new ResourceLocation(resLoc), u, v, width, height);
    }

    public TextureInfo(ResourceLocation res, int u, int v, int width, int height) {
        this.resource = res;
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
    }
}

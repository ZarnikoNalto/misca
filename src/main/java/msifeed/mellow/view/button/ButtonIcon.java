package msifeed.mellow.view.button;

import msifeed.mellow.render.RenderParts;
import msifeed.mellow.render.RenderShapes;
import msifeed.mellow.utils.Geom;
import net.minecraft.util.ResourceLocation;

public class ButtonIcon extends Button {
    protected ResourceLocation icon;
    protected int iconSize = 16;
    protected int textureWidth = 16;
    protected int textureHeight = 16;

    protected boolean enabled = true;
    protected Runnable callback = () -> {};

    public ButtonIcon(ResourceLocation icon, int textureWidth, int textureHeight) {
        setIcon(icon, textureWidth, textureHeight);
        setSize(iconSize, iconSize);
    }

    @Override
    public void setSize(int w, int h) {
        super.setSize(w, h);
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public void setIcon(ResourceLocation icon, int textureWidth, int textureHeight) {
        this.icon = icon;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    public void render(Geom geom) {
        if (isVisible()) {
            final int color = isEnabled() && isHovered() ? colorHover : colorNormal;
            RenderShapes.rect(geom, color);

            RenderParts.texture(geom, icon, iconSize, iconSize, textureWidth, textureHeight);
        }
    }
}

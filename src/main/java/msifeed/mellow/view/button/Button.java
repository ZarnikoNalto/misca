package msifeed.mellow.view.button;

import msifeed.mellow.render.RenderParts;
import msifeed.mellow.render.RenderShapes;
import msifeed.mellow.render.RenderUtils;
import msifeed.mellow.utils.Geom;
import msifeed.mellow.view.InputHandler;
import msifeed.mellow.view.View;
import net.minecraft.client.Minecraft;

public abstract class Button extends View implements InputHandler.MouseClick {
    protected int colorNormal = 0xbb000000;
    protected int colorHover = 0xbb404040;

    protected boolean enabled = true;
    protected Runnable callback = () -> {};

    @Override
    public void setSize(int w, int h) {
        super.setSize(w, h);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setCallback(Runnable callback) {
        this.callback = callback;
    }

    @Override
    public void render(Geom geom) {
        if (isVisible()) {
            final int color = isEnabled() && isHovered() ? colorHover : colorNormal;
            RenderShapes.rect(geom, color);
        }
    }

    @Override
    public void onMouseClick(int mouseX, int mouseY, int button) {
        if (isEnabled() && isVisible()) {
            callback.run();
        }
    }
}

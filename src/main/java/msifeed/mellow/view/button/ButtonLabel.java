package msifeed.mellow.view.button;

import msifeed.mellow.render.RenderParts;
import msifeed.mellow.render.RenderShapes;
import msifeed.mellow.render.RenderUtils;
import msifeed.mellow.utils.Geom;
import msifeed.mellow.view.InputHandler;
import msifeed.mellow.view.View;
import net.minecraft.client.Minecraft;

public class ButtonLabel extends Button {
    protected String text = "";
    protected RenderParts.TextPref pref = new RenderParts.TextPref();
    protected Geom textOffset = new Geom(2, 2, 0, 0);
    protected int textWidth = 0;

    protected boolean enabled = true;
    protected Runnable callback = () -> {};

    public ButtonLabel(String text) {
        setText(text);
        setSize(textWidth, RenderUtils.lineHeight() + 2);
    }

    @Override
    public void setSize(int w, int h) {
        super.setSize(w, h);
        textOffset.setPos((w - textWidth) / 2, (h - RenderUtils.lineHeight()) / 2 + 1);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        this.textWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
    }

    @Override
    public void render(Geom geom) {
        if (isVisible()) {
            final int color = isEnabled() && isHovered() ? colorHover : colorNormal;
            RenderShapes.rect(geom, color);

            final Geom textGeom = geom.add(textOffset);
            RenderParts.string(text, textGeom, 0xffffffff, pref);
        }
    }
}

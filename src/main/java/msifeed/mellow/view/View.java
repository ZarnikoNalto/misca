package msifeed.mellow.view;

import msifeed.mellow.FocusState;
import msifeed.mellow.utils.Geom;

public abstract class View {
    protected final Geom geometry = new Geom();
    protected boolean visible = true;

    public abstract void render(Geom geom);

    public Geom getBaseGeom() {
        return geometry;
    }

    public Geom getRenderGeom() {
        return geometry.clone();
    }

    public final void translate(int x, int y, int z) {
        setPos(geometry.x + x, geometry.y + y, geometry.z + z);
    }

    public final void grow(int w, int h) {
        setSize(geometry.w + w, geometry.h + h);
    }

    public void setPos(int x, int y, int z) {
        geometry.setPos(x, y, z);
    }

    public void setSize(int w, int h) {
        geometry.setSize(w, h);
    }

    public void setVisible(boolean visible) { this.visible = visible; }

    public boolean isFocusable() {
        return false;
    }

    public boolean isFocused() {
        return FocusState.INSTANCE.isFocused(this);
    }

    public boolean isHovered() {
        return FocusState.INSTANCE.isHovered(this);
    }

    public boolean isVisible() { return visible; }
}

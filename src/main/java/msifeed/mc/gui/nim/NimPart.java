package msifeed.mc.gui.nim;

public abstract class NimPart {
    private static NimPart focus = null;

    protected int posX, posY, posZ;
    protected int width, height;

    public NimPart() {

    }

    public int getX() {
        return posX;
    }

    public int getY() {
        return posY;
    }

    public int getZ() {
        return posZ;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void locate(int x, int y) {
        this.posX = x;
        this.posY = y;
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public abstract void render();

    public boolean inFocus() {
        return this == focus;
    }

    public void takeFocus() {
        focus = this;
    }

    public void releaseFocus() {
        focus = null;
    }
}

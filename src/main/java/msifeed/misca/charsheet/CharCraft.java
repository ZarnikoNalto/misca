package msifeed.misca.charsheet;

import net.minecraft.util.text.translation.I18n;

public enum CharCraft {
    arc,
    mys,
    enc,
    alc,
    eng,
    mnf,
    inf,
    chm,
    smt;

    public String tr() {
        return I18n.translateToLocal("enum.misca.craft." + name());
    }
}

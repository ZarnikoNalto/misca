package msifeed.misca.charsheet;

import msifeed.misca.charsheet.cap.CharsheetProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.translation.I18n;

public enum CharNeed {
    INT, SAN, STA;

    public double gainFactor(EntityPlayer player) {
        return CharsheetProvider.get(player).needsGain().get(this);
    }

    public double lostFactor(EntityPlayer player) {
        return CharsheetProvider.get(player).needsLost().get(this);
    }

    public String tr() {
        return I18n.translateToLocal("enum.misca.need." + name().toLowerCase());
    }
}

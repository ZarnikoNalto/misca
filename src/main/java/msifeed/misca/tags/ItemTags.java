package msifeed.misca.tags;

import net.minecraft.util.text.translation.I18n;

import java.util.EnumSet;

public enum ItemTags {
    fitness, perception, agility, coordination, ingenuity, tech, magic, charisma, composure,
    veryslow, slow, normal, fast, veryfast,
    shield, towershield,
    onehanded, twohanded,
    throwable,
    special,
    polearm;

    public boolean isSpeed() {
        return EnumSet.of(veryslow, slow, normal, fast, veryfast).contains(this);
    }

    public boolean isSkill() {
        return EnumSet.of(fitness, perception, agility, coordination, ingenuity, tech, magic, charisma, composure).contains(this);
    }

    public boolean isShield() {
        return EnumSet.of(shield, towershield).contains(this);
    }

    public boolean isHand() {
        return EnumSet.of(onehanded, twohanded).contains(this);
    }

    public boolean isOther() {
        return EnumSet.of(throwable, special, polearm).contains(this);
    }

    public String tr() {
        return I18n.translateToLocal("enum.misca.tag." + name());
    }
}

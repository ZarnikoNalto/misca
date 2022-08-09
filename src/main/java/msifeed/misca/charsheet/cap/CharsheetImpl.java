package msifeed.misca.charsheet.cap;

import msifeed.misca.charsheet.*;
import msifeed.sys.cap.FloatContainer;
import msifeed.sys.cap.IntContainer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.potion.Potion;

import java.util.HashMap;
import java.util.Map;

public class CharsheetImpl implements ICharsheet {
    private String name = "";
    private final IntContainer<CharSkill> skills = new IntContainer<>(CharSkill.class, 0, 25);
    private final IntContainer<CharCraft> crafts = new IntContainer<>(CharCraft.class, 0, 50);
    private final FloatContainer<CharNeed> needsRest = new FloatContainer<>(CharNeed.class, 1, 0, 10);
    private final FloatContainer<CharNeed> needsLost = new FloatContainer<>(CharNeed.class, 1, 0, 10);
    private final HashMap<Potion, Integer> potions = new HashMap<>();
    private final HashMap<Enchantment, Integer> enchants = new HashMap<>();
    private long lastUpdated = System.currentTimeMillis() / 1000;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        if (name.length() > MAX_NAME_LENGTH)
            this.name = name.substring(0, MAX_NAME_LENGTH);
        else
            this.name = name;
    }

    @Override
    public IntContainer<CharSkill> skills() {
        return skills;
    }

    @Override
    public IntContainer<CharCraft> crafts() {
        return crafts;
    }

    @Override
    public FloatContainer<CharNeed> needsGain() {
        return needsRest;
    }

    @Override
    public FloatContainer<CharNeed> needsLost() {
        return needsLost;
    }

    @Override
    public Map<Potion, Integer> potions() {
        return potions;
    }

    @Override
    public Map<Enchantment, Integer> enchants() {
        return enchants;
    }

    @Override
    public long getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public void setLastUpdated(long value) {
        this.lastUpdated = value;
    }

    @Override
    public void replaceWith(ICharsheet charsheet) {
        name = charsheet.getName();
        skills.replaceWith(charsheet.skills());
        needsRest.replaceWith(charsheet.needsGain());
        potions.clear();
        potions.putAll(charsheet.potions());
        enchants.clear();
        enchants.putAll(charsheet.enchants());
        lastUpdated = charsheet.getLastUpdated();
    }

    @Override
    public ICharsheet clone() {
        final CharsheetImpl clone;
        try {
            clone = (CharsheetImpl) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
        return clone;
    }
}

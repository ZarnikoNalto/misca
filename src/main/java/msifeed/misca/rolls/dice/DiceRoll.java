package msifeed.misca.rolls.dice;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.translation.I18n;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DiceRoll extends DiceMember {
    final private ArrayList<Dice> dices = new ArrayList<>();
    final private ArrayList<DiceMod> mods = new ArrayList<>();
    private Dice firstDice;
    private boolean isCrit = false;
    public Predicate<DiceRoll> critCondition = diceRoll -> false;
    public Consumer<DiceRoll> critEffect = diceRoll -> {};

    public DiceRoll(Dice... dices) {
        for (Dice dice : dices) {
            addDice(dice);
        }
    }

    public void addDice(Dice dice) {
        if (dice.isFirst()) {
            dice.setFirst(false);
        }

        if (dices.size() == 0) {
            setFirstDice(dice);
        }

        this.dices.add(dice);
    }

    public void addMod(int mod) {
        mods.add(new DiceMod(mod));
    }

    public void addMod(int mod, String source) {
        mods.add(new DiceMod(mod, source));
    }

    public void addMod(DiceMod diceMod) {
        mods.add(diceMod);
    }

    public void addRoll(DiceRoll diceRoll) {
        for (Dice dice : diceRoll.getDices()) {
            addDice(dice);
        }

        for (DiceMod diceMod : diceRoll.getMods()) {
            addMod(diceMod);
        }
    }

    public void roll(boolean reroll) {
        result = 0;

        for (Dice dice : dices) {
            if (dice.getResult() == null || reroll) {
                dice.roll(false);
            }

            result += dice.getResult();
        }

        for (DiceMod mod : mods) {
            result += mod.getResult();
        }

        if (!isCrit && critCondition.test(this)) {
            isCrit = true;
            critEffect.accept(this);
        }

        if (negative) {
            result = -result;
        }
    }

    public void roll() {
        roll(false);
    }

    public ArrayList<Dice> getDices() {
        return dices;
    }

    public ArrayList<DiceMod> getMods() {
        return mods;
    }

    public Dice getFirstDice() {
        return firstDice;
    }

    public void setFirstDice(Dice firstDice) {
        if (this.firstDice != null) {
            this.firstDice.setFirst(false);
        }

        this.firstDice = firstDice;
        firstDice.setFirst(true);
    }

    public boolean isCrit() {
        return isCrit;
    }

    public int getMax() {
        int max = 0;

        for (Dice dice : dices) {
            max += dice.getMax();
        }

        for (DiceMod mod : mods) {
            max += mod.getResult();
        }

        if (negative) {
            max = -max;
        }

        return max;
    }

    public int getMin() {
        int min = 0;

        for (Dice dice : dices) {
            min += dice.getMin();
        }

        for (DiceMod mod : mods) {
            min += mod.getResult();
        }

        if (negative) {
            min = -min;
        }

        return min;
    }

    public void addTooltip(ITextComponent textComponent) {
        final ITextComponent tooltip = new TextComponentString(String.format("%s = \u00A7l%d", this, result));

        if (isCrit) {
            tooltip.appendText(String.format("\n\u00A7l\u00A76%s\n", I18n.translateToLocal("misca.roll.crit")));
        }

        if (!hidden) {
            for(Dice dice : dices) {
                if (dice.isHidden()) {
                    continue;
                }

                tooltip.appendText(dice.getTooltipLine());
            }

            for (DiceMod mod : mods) {
                if (mod.isHidden()) {
                    continue;
                }

                tooltip.appendText("\n" + mod.formatted());
            }
        }

        textComponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip));
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        for(Dice dice : dices) {
            str.append(dice.toString());
        }

        for (DiceMod mod : mods) {
            str.append(mod.toString());
        }

        return String.format((!getName().isEmpty() && !isHidden() ? getName() + " " : "") + "%s", str);
    }
}

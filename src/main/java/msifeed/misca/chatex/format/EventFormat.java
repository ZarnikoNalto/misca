package msifeed.misca.chatex.format;

import msifeed.misca.rolls.dice.DiceRoll;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class EventFormat {
    public static ITextComponent dice(EntityPlayer sender, DiceRoll diceRoll) {
        final ITextComponent tc = new TextComponentTranslation("misca.chatex.dice",
                sender.getDisplayName(),
                !diceRoll.getName().isEmpty() ? diceRoll.getName() : diceRoll.toString(),
                (diceRoll.isCrit() ? "\u00A7l" : "") + diceRoll.getResult());
        tc.getStyle().setColor(TextFormatting.GOLD);
        diceRoll.addTooltip(tc);
        return tc;
    }

    public static ITextComponent message(EntityPlayer sender, String msg) {
        final ITextComponent tc = new TextComponentTranslation(msg, sender.getDisplayName());
        tc.getStyle().setColor(TextFormatting.GOLD);

        return tc;
    }
}

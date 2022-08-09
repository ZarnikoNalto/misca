package msifeed.misca.cmd;

import msifeed.misca.chatex.ChatexRpc;
import msifeed.misca.rolls.dice.DiceRoll;
import msifeed.misca.rolls.Dices;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class RollCommand extends CommandBase {
    @Override
    public String getName() {
        return "roll";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/roll 3d7-d8+5";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        final String joined = String.join("", args);
        final DiceRoll diceRoll;

        try {
            diceRoll = Dices.parseThrow(joined);
            diceRoll.roll();
        } catch (NumberInvalidException e) {
            sender.sendMessage(new TextComponentString("invalid input"));
            return;
        }

        if (sender instanceof EntityPlayerMP) {
            ChatexRpc.broadcastDiceRoll((EntityPlayerMP) sender, diceRoll);
        } else {
            final String name = sender.getDisplayName().getFormattedText();
            final String msg = String.format("[ROLL] %s: %s = %s", name, diceRoll, diceRoll.getResult());
            final ITextComponent message = new TextComponentString(msg);
            diceRoll.addTooltip(message);
            sender.sendMessage(message);
        }
    }
}

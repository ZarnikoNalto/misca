package msifeed.misca.cmd;

import msifeed.misca.MiscaPerms;
import msifeed.misca.charsheet.CharNeed;
import msifeed.misca.charstate.cap.CharstateProvider;
import msifeed.misca.charstate.cap.ICharstate;
import msifeed.misca.charstate.handler.IntegrityHandler;
import msifeed.misca.charstate.handler.SanityHandler;
import msifeed.misca.charstate.handler.StaminaHandler;
import msifeed.misca.logdb.LogDB;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class NeedsCommand extends CommandBase {
    @Override
    public String getName() {
        return "needs";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/needs <who> <int san sta> [<add set tolerance> <value>]";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        switch (args.length) {
            case 1:
                return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
            case 2:
                return getListOfStringsMatchingLastWord(args,"int", "san", "sta");
            case 3:
                return getListOfStringsMatchingLastWord(args,"add", "set", "tolerance");
            default:
                return Collections.emptyList();
        }
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) throw new SyntaxErrorException("Expected at least 2 args");

        final EntityPlayerMP player = getPlayer(server, sender, args[0]);
        final IAttribute attr = getAttribute(args[1]);
        final IAttributeInstance inst = player.getEntityAttribute(attr);

        if (args.length >= 3 && args[2].equalsIgnoreCase("tolerance")) {
            final ICharstate state = CharstateProvider.get(player);
            final CharNeed need = getNeed(args[1]);
            final int tolerance = (int) (state.tolerances().get(need) * 100);

            sender.sendMessage(new TextComponentString("Tolerance to " + need + " is " + tolerance + "%"));

            if (args.length >= 4 && MiscaPerms.isGameMaster(sender)) {
                final double value = (float) parseDouble(args[3], 0, 1);
                state.tolerances().set(need, (float) value);
                sender.sendMessage(new TextComponentString("  and now is set to " + (int) (value * 100) + "%"));
            }
        } else if (args.length >= 4 && MiscaPerms.isGameMaster(sender)) {
            final boolean set = args[2].equalsIgnoreCase("set");
            final double value = (float) parseDouble(args[3], -200, 200);
            final double curr = inst.getBaseValue();
            final double modified = attr.clampValue(set ? value : value + curr);

            inst.setBaseValue(modified);
            final String msg = String.format("Change %s's %s from %.3f to %.3f", player.getName(), args[1], curr, modified);
            sender.sendMessage(new TextComponentString(msg));
            LogDB.INSTANCE.log(sender, "need", msg);
        } else {
            final String rep = String.format("%s's %s: %.3f (base %.3f)", player.getDisplayNameString(), args[1], inst.getAttributeValue(), inst.getBaseValue());
            sender.sendMessage(new TextComponentString(rep));
        }
    }

    private static IAttribute getAttribute(String name) throws CommandException {
        switch (name) {
            case "int":
            case "integrity":
                return IntegrityHandler.INTEGRITY;
            case "san":
            case "sanity":
                return SanityHandler.SANITY;
            case "sta":
            case "stamina":
                return StaminaHandler.STAMINA;
            default:
                throw new SyntaxErrorException("Invalid need name");
        }
    }

    private static CharNeed getNeed(String name) throws CommandException {
        switch (name) {
            case "int":
            case "integrity":
                return CharNeed.INT;
            case "san":
            case "sanity":
                return CharNeed.SAN;
            case "sta":
            case "stamina":
                return CharNeed.STA;
            default:
                throw new SyntaxErrorException("Invalid need name");
        }
    }
}

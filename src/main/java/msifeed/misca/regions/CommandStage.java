package msifeed.misca.regions;

import msifeed.misca.MiscaPerms;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.server.command.CommandTreeBase;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandStage extends CommandTreeBase {
    @Override
    public String getName() {
        return "stage";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/regions stage <add remove>";
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return MiscaPerms.check(sender, "misca.regions.stage");
    }

    public CommandStage() {
        addSubcommand(new Add());
        addSubcommand(new Remove());
    }

    private static class Add extends CommandBase {
        @Override
        public String getName() {
            return "add";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "add <name> <stage>";
        }

        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
            return MiscaPerms.check(sender, "misca.regions.stage.add");
        }

        @Override
        public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
            if (args.length == 1) {
                final int dim = sender.getEntityWorld().provider.getDimension();
                return RegionControl.config().get(dim).stream()
                        .map(r -> r.name)
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            if (args.length < 2) throw new CommandException("Usage: add <name> <stage>");

            final int dim = sender.getEntityWorld().provider.getDimension();
            final String name = args[0];
            final RegionConfig.Region region = RegionControl.config().get(dim, name);
            if (region == null) throw new CommandException("Unknown region");

            final String stage = args[1];

            if (region.stages.contains(stage)) {
                sender.sendMessage(new TextComponentString("Region already contains this stage"));
                return;
            }

            region.stages.add(stage);

            try {
                RegionControl.writeConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }

            sender.sendMessage(new TextComponentString(String.format("Added stage '%s' to region '%s'", stage, name)));
        }
    }

    private static class Remove extends CommandBase {
        @Override
        public String getName() {
            return "remove";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "remove <name> <stage>";
        }

        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
            return MiscaPerms.check(sender, "misca.regions.stage.remove");
        }

        @Override
        public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
            final int dim = sender.getEntityWorld().provider.getDimension();
            if (args.length == 1) {
                return RegionControl.config().get(dim).stream()
                        .map(r -> r.name)
                        .collect(Collectors.toList());
            }
            if (args.length == 2) {
                final RegionConfig.Region region = RegionControl.config().get(dim, args[0]);
                if (region != null)
                    return new ArrayList<>(region.stages);
            }
            return Collections.emptyList();
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            if (args.length < 2) throw new CommandException("Usage: remove <name> <stage>");

            final int dim = sender.getEntityWorld().provider.getDimension();
            final String name = args[0];
            final RegionConfig.Region region = RegionControl.config().get(dim, name);
            if (region == null) throw new CommandException("Unknown region");

            final String stage = args[1];

            if (!region.stages.contains(stage)) {
                sender.sendMessage(new TextComponentString("Region does not contain this stage"));
                return;
            }

            region.stages.remove(stage);

            try {
                RegionControl.writeConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }

            sender.sendMessage(new TextComponentString(String.format("Removed stage '%s' from region '%s'", stage, name)));
        }
    }
}

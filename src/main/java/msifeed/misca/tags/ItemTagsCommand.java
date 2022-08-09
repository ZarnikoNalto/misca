package msifeed.misca.tags;

import msifeed.misca.MiscaPerms;
import msifeed.misca.MiscaThings;
import msifeed.misca.charstate.handler.StaminaHandler;
import msifeed.misca.supplies.ItemSuppliesBeacon;
import msifeed.misca.supplies.SuppliesFlow;
import msifeed.misca.supplies.cap.ISuppliesInvoice;
import msifeed.misca.supplies.cap.SuppliesInvoiceProvider;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.ForgeHooks;
import org.apache.commons.lang3.EnumUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemTagsCommand extends CommandBase {
    private static final Set<String> tags = Stream.of(ItemTags.values())
            .map(Enum::name)
            .collect(Collectors.toSet());
    private static final String validTags = "Valid Tags: " + Stream.of(ItemTags.values())
            .map(Enum::name)
            .collect(Collectors.joining(", "));

    @Override
    public String getName() {
        return "itemtags";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/itemtags\n" +
                "  add <tag>\n" +
                "  remove <tag>\n" +
                "  clear\n" +
                "  OR show existing tags if empty";
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return MiscaPerms.isGameMaster(sender);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "add", "remove", "clear");
        if (args.length == 2)
            return getListOfStringsMatchingLastWord(args, tags);
        else
            return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayerMP))
            return;

        final EntityPlayerMP player = (EntityPlayerMP) sender;
        final ItemStack stack = player.getHeldItemMainhand();

        if (stack == ItemStack.EMPTY) {
            player.sendStatusMessage(new TextComponentString("You must hold item in hand"), false);
            return;
        }

        if (args.length == 0) {
            handleShowTags(player, stack);
        } else if (args[0].equals("add") && args.length >= 2) {
            handleAddTag(player, stack, args[1]);
        } else if (args[0].equals("remove")  && args.length >= 2) {
            handleRemoveTag(player, stack, args[1]);
        } else if (args[0].equals("clear")) {
            handleClearTags(player, stack);
        } else {
            player.sendStatusMessage(new TextComponentString(getUsage(sender)), false);
        }
    }

    private void handleShowTags(EntityPlayerMP player, ItemStack stack) {
        final NBTTagCompound nbt = stack.getTagCompound();

        if (nbt == null || !nbt.hasKey("tags")) {
            player.sendStatusMessage(new TextComponentString("This item has no tags"), false);
            return;
        };

        player.sendStatusMessage(new TextComponentString(nbt.getTagList("tags", 8).toString()), false);
    }

    private void handleAddTag(EntityPlayerMP player, ItemStack stack, String arg) {
        arg = arg.toLowerCase(Locale.ROOT);

        if (!EnumUtils.isValidEnum(ItemTags.class, arg)) {
            player.sendStatusMessage(new TextComponentString(validTags), false);
            return;
        }

        NBTTagCompound nbt = stack.getTagCompound();
        NBTTagList tags;

        if (nbt == null || !nbt.hasKey("tags")) {
            stack.setTagCompound(new NBTTagCompound());
            nbt = stack.getTagCompound();
            tags = new NBTTagList();
        } else {
            tags = nbt.getTagList("tags", 8);
        }

        for (NBTBase base : tags) {
            NBTTagString tag = (NBTTagString) base;

            if (tag.getString().equals(arg)) {
                return;
            }
        }

        tags.appendTag(new NBTTagString(arg));
        nbt.setTag("tags", tags);
    }

    private void handleRemoveTag(EntityPlayerMP player, ItemStack stack, String arg) {
        arg = arg.toLowerCase(Locale.ROOT);

        if (!EnumUtils.isValidEnum(ItemTags.class, arg)) {
            player.sendStatusMessage(new TextComponentString(validTags), false);
            return;
        }

        NBTTagCompound nbt = stack.getTagCompound();
        NBTTagList tags;

        if (nbt == null || !nbt.hasKey("tags")) {
            stack.setTagCompound(new NBTTagCompound());
            nbt = stack.getTagCompound();
            tags = new NBTTagList();
        } else {
            tags = nbt.getTagList("tags", 8);
        }

        if (tags.tagCount() == 0) {
            return;
        }

        for (int i = 0; i < tags.tagCount(); i++) {
            NBTTagString tag = (NBTTagString) tags.get(i);

            if (tag.getString().equals(arg)) {
                tags.removeTag(i);
                break;
            }
        }

        if (tags.tagCount() == 0) {
            nbt.removeTag("tags");
        } else {
            nbt.setTag("tags", tags);
        }
    }

    private void handleClearTags(EntityPlayerMP player, ItemStack stack) {
        final NBTTagCompound tags = stack.getTagCompound();

        if (tags != null) {
            tags.removeTag("tags");
        }
    }
}

package msifeed.misca.locks.items;

import msifeed.misca.Misca;
import msifeed.misca.locks.LockUtils;
import msifeed.misca.locks.Locks;
import msifeed.misca.locks.LocksConfig;
import msifeed.misca.locks.cap.key.ILockKey;
import msifeed.misca.locks.cap.key.LockKeyProvider;
import msifeed.misca.locks.cap.keyring.IKeyRing;
import msifeed.misca.locks.cap.keyring.KeyRingProvider;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;

public class ItemKeyRing extends Item implements IUnlockTool {
    public static final String ID = "key_ring";

    public ItemKeyRing() {
        setRegistryName(Misca.MODID, ID);
        setTranslationKey(ID);
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!player.isSneaking()) return EnumActionResult.PASS;

        final IKeyRing keyRing = KeyRingProvider.get(player.getHeldItem(hand));
        if (keyRing != null && Locks.toggleLock(world, pos, keyRing)) {
            return EnumActionResult.SUCCESS;
        } else {
            sendStatus(player, "Can't toggle the lock", TextFormatting.RED);
            return EnumActionResult.FAIL;
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (player.isSneaking()) return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));

        final IKeyRing keyRing = KeyRingProvider.get(player.getHeldItem(hand));
        if (keyRing != null) {
            final NonNullList<ItemStack> keys = keyRing.getKeys();
            if (keys.isEmpty()) {
                sendStatus(player, "Key Ring Is Empty!", TextFormatting.WHITE);
            } else {
                final ItemStack lastKey = keys.get(keys.size() - 1);

                if (!player.addItemStackToInventory(lastKey.copy())) {
                    player.dropItem(lastKey, true);
                }

                keyRing.remove(lastKey);

                sendStatus(player, "Removed Key: " + lastKey.getDisplayName(), TextFormatting.WHITE);
            }
        } else {
            return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
        }

        return new ActionResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        final IKeyRing keyRing = KeyRingProvider.get(stack);
        if (keyRing != null) {
            final List<ItemStack> keys = keyRing.getKeys();
            final LocksConfig config = Misca.getSharedConfig().locks;

            if (!keys.isEmpty()) {
                tooltip.add("Keys (" + keys.size() + "/" + config.keyRingMaxKeys + "):");

                for (ItemStack keyStack : keys) {
                    final ILockKey key = LockKeyProvider.get(keyStack);
                    if (key == null)
                        continue;
                    tooltip.add(keyStack.getDisplayName() + " - " + LockUtils.toHex(key.getSecret()));
                }
            }
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        final KeyRingProvider provider = new KeyRingProvider();
        if (nbt != null) provider.deserializeNBT(nbt.getTag("KeyRing"));
        return provider;
    }

    @Nullable
    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        final IKeyRing keyRing = KeyRingProvider.get(stack);
        final NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        nbt.setTag("KeyRing", KeyRingProvider.CAP.writeNBT(keyRing, null));
        return nbt;
    }

    @Override
    public void readNBTShareTag(ItemStack stack, @Nullable NBTTagCompound nbt) {
        stack.setTagCompound(nbt);
        if (nbt != null) {
            final IKeyRing keyRing = KeyRingProvider.get(stack);
            KeyRingProvider.CAP.readNBT(keyRing, null, nbt.getTag("KeyRing"));
        }
    }

    private static void sendStatus(EntityPlayer player, String message, TextFormatting color) {
        final ITextComponent te = new TextComponentString(message);
        te.getStyle().setColor(color);
        player.sendStatusMessage(te, true);
    }
}

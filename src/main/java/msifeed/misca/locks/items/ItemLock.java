package msifeed.misca.locks.items;

import msifeed.misca.Misca;
import msifeed.misca.locks.*;
import msifeed.misca.locks.cap.ILockHolder;
import msifeed.misca.locks.cap.LockAccessor;
import msifeed.misca.locks.cap.lock.ILock;
import msifeed.misca.locks.cap.lock.LockProvider;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureStrongholdPieces;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemLock extends Item {
    public static final String ID_BASE = "lock_";

    private String secret = "";

    private final LockType type;
    private ItemStack lockStack;
    private EntityPlayer player;
    private World worldIn;
    private BlockPos pos;

    private BlockPos handPos;
    private World handWorld;
    private EntityPlayer handPlayer;


    public ItemLock(LockType type) {
        this.type = type;

        setRegistryName(Misca.MODID, ID_BASE + type.name());
        setTranslationKey(ID_BASE + type.name());
        setHasSubtypes(true);
        setCreativeTab(CreativeTabs.TOOLS);
        setContainerItem(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static ItemStack createLock(LockType type, int secret) {
        return createLock(type.getItem(), secret);
    }

    public static ItemStack createLock(Item item, int secret) {
        final ItemStack stack = new ItemStack(item, 1, 1);
        final ILock lock = LockProvider.get(stack);
        lock.setSecret(secret);
        return stack;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        this.player = player;
        this.worldIn = worldIn;
        this.pos = pos;


        if (!player.isSneaking()) return EnumActionResult.PASS;

        if (player.world.isRemote) {
            if (LockAccessor.isLocked(worldIn, pos)) return EnumActionResult.FAIL;
            else return EnumActionResult.SUCCESS;
        }

        //Определяем тип замка
        LockType type = this.type;
        lockStack = player.getHeldItem(hand);


        EnumActionResult routeResult = EnumActionResult.FAIL;
        switch (type) {
            case mechanical:
                routeResult = mechanicalLockRoute();
                break;
            case digital:
                routeResult = startDigitalRoute();
                if (routeResult == EnumActionResult.FAIL) return routeResult;
                break;
        }

        return routeResult;
    }

    private EnumActionResult mechanicalLockRoute() {
        //Проверяем, есть ли уже замок на двери
        final ILockHolder currentLock = LockAccessor.createWrap(worldIn, pos);

        final LocksConfig config = Misca.getSharedConfig().locks;

        boolean isCurrentLock = currentLock != null;
        final boolean isBlank = isBlank(lockStack);
        final int secret = !isBlank ? LockProvider.get(lockStack).getSecret()
                : LockUtils.generateSecret(8, 15);

        if (isCurrentLock && !currentLock.isLocked() && currentLock.getSecret() != 0) {
            final ItemStack oldLockStack = ItemLock.createLock(currentLock.getType(), currentLock.getSecret());
            if (!player.addItemStackToInventory(oldLockStack)) {
                player.dropItem(oldLockStack, true);
            }

            currentLock.removeLock();
        }

        if (!Locks.addLock(worldIn, pos, type, secret)) {
            sendStatus(player, "Failed to set lock", TextFormatting.RED);
            return EnumActionResult.FAIL;
        }

        lockStack.shrink(1);

        if (isBlank) {
            final ItemStack keys = ItemKey.createKey(secret);
            keys.setCount(config.setupKeysCount);

            if (!player.addItemStackToInventory(keys)) {
                player.dropItem(keys, true);
            }
        }


        //Выводит текст
        sendStatus(player, "Lock set " + LockUtils.toHex(secret), TextFormatting.GREEN);
        return EnumActionResult.SUCCESS;
    }

    private EnumActionResult startDigitalRoute() {
        //Проверяем, есть ли уже замок на двери
        final ILockHolder currentLock = LockAccessor.createWrap(worldIn, pos);
        if (currentLock.getSecret() != 0 && !currentLock.isLocked())
            return EnumActionResult.FAIL; //Если уже есть замок, поверх него нельзя поставить электронный

        awakeDigitalGui(GuiDigitalLock.GuiType.LOCK);
        return EnumActionResult.PASS;
    }

    private void finishDigitalRoute() {
        //Проверяем, есть ли уже замок на двери
        final ILockHolder currentLock = LockAccessor.createWrap(worldIn, pos);
        final LocksConfig config = Misca.getSharedConfig().locks;

        int int_secret = Integer.parseInt(secret);

        if (!Locks.addLock(worldIn, pos, type, int_secret)) {
            sendStatus(player, "Failed to set lock", TextFormatting.RED);
            return;
        }

        lockStack.shrink(1);
        sendStatus(player, "Lock set " + LockUtils.toHex(int_secret), TextFormatting.GREEN);
    }


    private void awakeDigitalGui(GuiDigitalLock.GuiType type) {
        Minecraft.getMinecraft().displayGuiScreen(new GuiDigitalLock(this, type));
    }

    @SideOnly(Side.CLIENT)
    public boolean onCloseGui(boolean isNormal, String result) {
        if (!isNormal) {
            sendStatus(Minecraft.getMinecraft().player, "Menu closed!", TextFormatting.RED);
            return false;
        }

        secret = result;
        int int_secret = 0;
        try {
            int_secret = Integer.parseInt(secret);
        } catch (Exception e) {
        }
        sendStatus(Minecraft.getMinecraft().player, "Password: " + LockUtils.toHex(int_secret), TextFormatting.GREEN);


        finishDigitalRoute();
        return true;
    }


    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        final ILock lock = LockProvider.get(stack);
        tooltip.add("Secret: " + LockUtils.toHex(lock.getSecret()));
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        final LockProvider provider = new LockProvider();
        if (nbt != null) provider.deserializeNBT(nbt.getTag("Lock"));
        return provider;
    }

    @Nullable
    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        final ILock lock = LockProvider.get(stack);
        final NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        nbt.setTag("Lock", LockProvider.CAP.writeNBT(lock, null));
        return nbt;
    }

    @Override
    public void readNBTShareTag(ItemStack stack, @Nullable NBTTagCompound nbt) {
        stack.setTagCompound(nbt);
        if (nbt != null) {
            final ILock lock = LockProvider.get(stack);
            LockProvider.CAP.readNBT(lock, null, nbt.getTag("Lock"));
        }
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack stack) {
        final ILock lock = LockProvider.get(stack);
        return createLock(stack.getItem(), lock.getSecret());
    }


    /**
     * Определяет состояние использованного замка.
     * <p>
     * ...Если у замка секрет != 0 -> isBlank = false. Так как замок не "пустой"
     * </p>
     * ...Если у замка секрет == 0 -> isBlank = true. Так как замок еще не инициализирован
     *
     * <p></p>
     * <p>
     * В stack возможны два случая:
     * </p>
     *
     * <p>
     * 1) Пользователь использовал стак "пустых" замков. Так как (в теории) только пустые замки могут быть в стаке. Для каждого из этих замков isBlank() = true
     * </p>
     * <p>
     * 2) Пользователь использовал один(стак из одного) непустого замка. Замок в стаке только один(в теории),так как он обладает уникальным секретом. isBlank() = false
     * </p>
     *
     * @param stack Стак замков.
     * @return Возвращает "состояние" замка(замков, если их много в стаке)
     */
    public static boolean isBlank(ItemStack stack) {
        return stack.getItemDamage() == 0;
    }

    private static void sendStatus(EntityPlayer player, String message, TextFormatting color) {
        final ITextComponent te = new TextComponentString(message);
        te.getStyle().setColor(color);
        player.sendStatusMessage(te, true);
    }


    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        handPos = event.getPos();
        handWorld = event.getWorld();
        handPlayer = event.getEntityPlayer();

        //TODO: ГДЕ-ТО ТУТ ДОЛЖНА БЫТЬ ПРОВЕРКА КНОПКИ МЫШИ

        ItemStack itemstack = event.getItemStack();


        if (event instanceof PlayerInteractEvent.RightClickBlock || event instanceof PlayerInteractEvent.RightClickItem || event instanceof PlayerInteractEvent.RightClickEmpty)
            if (event.getEntityPlayer().isSneaking())
                //Check to see if the player right-clicked
                if (event.getHand().equals(EnumHand.MAIN_HAND)) {

                    //Check if player has nothing in their hand
                    if (itemstack.isEmpty()) {

                        awakeDigitalGui(GuiDigitalLock.GuiType.KEY);

                    }
                }
    }

    public void tryApplyKey(boolean isNormal, String result) {
        if (!isNormal) return;

        World world = handWorld;
        BlockPos pos = handPos;
        EntityPlayer player = handPlayer;

        int int_secret = 0;
        if (result.trim() != "")
            int_secret = Integer.parseInt(result);

        if (Locks.toggleLock(world, pos, int_secret)) {
            final boolean lockState = LockAccessor.createWrap(world, pos).isLocked();
            if (lockState)
                sendStatus(player, "Closed the lock!", TextFormatting.GREEN);
            else
                sendStatus(player, "Opened the lock!", TextFormatting.GREEN);


            return;
        } else {
            sendStatus(player, "Invalid Password!", TextFormatting.RED);
            return;
        }
    }

}

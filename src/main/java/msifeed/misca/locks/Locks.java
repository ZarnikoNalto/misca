package msifeed.misca.locks;

import msifeed.misca.Misca;
import msifeed.misca.locks.cap.ILockHolder;
import msifeed.misca.locks.cap.LockAccessor;
import msifeed.misca.locks.cap.chunk.ChunkLockableImpl;
import msifeed.misca.locks.cap.chunk.ChunkLockableProvider;
import msifeed.misca.locks.cap.chunk.ChunkLockableStorage;
import msifeed.misca.locks.cap.chunk.IChunkLockable;
import msifeed.misca.locks.cap.key.ILockKey;
import msifeed.misca.locks.cap.key.LockKeyImpl;
import msifeed.misca.locks.cap.key.LockKeyStorage;
import msifeed.misca.locks.cap.keyring.IKeyRing;
import msifeed.misca.locks.cap.keyring.KeyRingImpl;
import msifeed.misca.locks.cap.keyring.KeyRingStorage;
import msifeed.misca.locks.cap.lock.ILock;
import msifeed.misca.locks.cap.lock.LockImpl;
import msifeed.misca.locks.cap.lock.LockStorage;
import msifeed.misca.locks.cap.lockable.ILockable;
import msifeed.misca.locks.cap.lockable.LockableImpl;
import msifeed.misca.locks.cap.lockable.LockableProvider;
import msifeed.misca.locks.cap.lockable.LockableStorage;
import msifeed.misca.locks.cap.pick.ILockPick;
import msifeed.misca.locks.cap.pick.LockPickImpl;
import msifeed.misca.locks.cap.pick.LockPickStorage;
import msifeed.misca.locks.items.ItemLock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Locks {

    public void preInit() {
        CapabilityManager.INSTANCE.register(ILockable.class, new LockableStorage(), LockableImpl::new);
        CapabilityManager.INSTANCE.register(IChunkLockable.class, new ChunkLockableStorage(), ChunkLockableImpl::new);
        CapabilityManager.INSTANCE.register(ILockKey.class, new LockKeyStorage(), LockKeyImpl::new);
        CapabilityManager.INSTANCE.register(ILockPick.class, new LockPickStorage(), LockPickImpl::new);
        CapabilityManager.INSTANCE.register(ILock.class, new LockStorage(), LockImpl::new);
        CapabilityManager.INSTANCE.register(IKeyRing.class, new KeyRingStorage(), KeyRingImpl::new);


        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new LockItems());
        Misca.RPC.register(new LocksRpc());
    }

    @SubscribeEvent
    public void onAttachTileCapability(AttachCapabilitiesEvent<TileEntity> event) {
        if (event.getObject() instanceof IInventory)
            event.addCapability(ILockable.KEY, new LockableProvider());
    }

    @SubscribeEvent
    public void onAttachChunkCapability(AttachCapabilitiesEvent<Chunk> event) {
        event.addCapability(ILockable.KEY, new ChunkLockableProvider());
    }

    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public void onChunkWatch(ChunkWatchEvent.Watch event) {
        if (event.getChunkInstance() != null) {
            LocksRpc.sendChunkLocks(event.getPlayer(), event.getChunkInstance());
        } else {
            // Add some silly loading
            event.getPlayer().sendStatusMessage(new TextComponentString("Woah! Chunk is not loaded! Loading ASAP!"), true);
            final ChunkProviderServer provider = (ChunkProviderServer) event.getPlayer().world.getChunkProvider();
            provider.loadChunk(event.getChunk().x, event.getChunk().z, () -> {
                LocksRpc.sendChunkLocks(event.getPlayer(), event.getChunkInstance());
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (LockAccessor.isLocked(event.getWorld(), event.getPos())) {
            event.setUseBlock(Event.Result.DENY);
            event.setUseItem(Event.Result.ALLOW);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        final Block block = event.getPlacedBlock().getBlock();
        final LocksConfig.Lookup lookup = LockAccessor.getBlockLookup(block);
        if (lookup != LocksConfig.Lookup.adjacent) return;

        if (hasAdjacentLockedBlock(event.getWorld(), event.getPos(), block)) {
            event.setCanceled(true);
        }
    }

    private boolean hasAdjacentLockedBlock(World world, BlockPos pos, Block block) {
        for (EnumFacing e : EnumFacing.HORIZONTALS) {
            final BlockPos offPos = pos.offset(e);
            final IBlockState state = world.getBlockState(offPos);
            if (!state.getBlock().equals(block)) continue;

            final TileEntity tile = world.getTileEntity(offPos);
            if (tile == null) continue;

            final ILockable lock = LockableProvider.get(tile);
            if (lock != null && lock.isLocked())
                return true;
        }

        return false;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        final World world = event.getWorld();
        final BlockPos pos = event.getPos();
        if (LockAccessor.isLocked(world, pos)) {
            event.setCanceled(true);
        } else {
            final ILockHolder lock = LockAccessor.createWrap(world, pos);
            if (lock != null && lock.getSecret() != 0) {
                world.spawnEntity(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), ItemLock.createLock(lock.getType(), lock.getSecret())));
            }

            removeLock(world, pos);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (LockAccessor.isLocked(event.getEntityPlayer().world, event.getPos())) {
            event.setCanceled(true);
        }
    }

    public static boolean toggleLock(World world, BlockPos pos, int key) {
        final ILockHolder lock = LockAccessor.createWrap(world, pos);

        if (lock != null && lock.hasSecret() && lock.canOpenWith(key)) {
            lock.setLocked(!lock.isLocked());
            return true;
        } else {
            return false;
        }
    }

    public static boolean toggleLock(World world, BlockPos pos, IKeyRing keyRing) {
        final ILockHolder lock = LockAccessor.createWrap(world, pos);

        if (lock != null && lock.hasSecret() && keyRing.checkSecret(lock.getSecret())) {
            lock.setLocked(!lock.isLocked());
            return true;
        } else {
            return false;
        }
    }

    public static boolean forceToggleLock(World world, BlockPos pos) {
        final ILockHolder lock = LockAccessor.createWrap(world, pos);

        if (lock != null && lock.hasSecret()) {
            lock.setLocked(!lock.isLocked());
            return true;
        } else {
            return false;
        }
    }

    public static boolean addLock(World world, BlockPos pos, LockType type, int secret) {
        final ILockHolder lock = LockAccessor.createWrap(world, pos);
        return lock != null && lock.addLock(type, secret);
    }

    public static boolean removeLock(World world, BlockPos pos) {
        final ILockHolder lock = LockAccessor.createWrap(world, pos);
        return lock != null && lock.removeLock();
    }
}

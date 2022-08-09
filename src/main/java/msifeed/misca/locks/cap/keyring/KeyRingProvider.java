package msifeed.misca.locks.cap.keyring;

import msifeed.misca.locks.cap.key.ILockKey;
import msifeed.misca.locks.cap.key.LockKeyProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class KeyRingProvider implements ICapabilitySerializable<NBTBase> {
    @CapabilityInject(IKeyRing.class)
    public static Capability<IKeyRing> CAP = null;

    private final Capability.IStorage<IKeyRing> storage = CAP.getStorage();
    private final IKeyRing instance = Objects.requireNonNull(CAP.getDefaultInstance());

    @Nullable
    public static IKeyRing get(ItemStack stack) {
        return stack.getCapability(KeyRingProvider.CAP, null);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CAP;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CAP ? CAP.cast(this.instance) : null;
    }

    @Override
    public NBTBase serializeNBT() {
        return storage.writeNBT(CAP, instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        storage.readNBT(CAP, instance, null, nbt);
    }
}

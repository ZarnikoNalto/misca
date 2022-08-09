package msifeed.misca.locks.cap.lock;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class LockStorage implements Capability.IStorage<ILock> {
    @Nullable
    @Override
    public NBTBase writeNBT(Capability<ILock> capability, ILock instance, EnumFacing side) {
        final NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("secret", instance.getSecret());

        return nbt;
    }

    @Override
    public void readNBT(Capability<ILock> capability, ILock instance, EnumFacing side, NBTBase nbtBase) {
        final NBTTagCompound nbt = (NBTTagCompound) nbtBase;
        if (nbt == null) return;

        instance.setSecret(nbt.getInteger("secret"));
    }
}

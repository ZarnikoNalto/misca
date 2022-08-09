package msifeed.misca.locks.cap.keyring;

import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class KeyRingStorage implements Capability.IStorage<IKeyRing> {
    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IKeyRing> capability, IKeyRing instance, EnumFacing side) {
        final NBTTagCompound nbt = new NBTTagCompound();
        ItemStackHelper.saveAllItems(nbt, instance.getKeys());

        return nbt;
    }

    @Override
    public void readNBT(Capability<IKeyRing> capability, IKeyRing instance, EnumFacing side, NBTBase nbtBase) {
        final NBTTagCompound nbt = (NBTTagCompound) nbtBase;
        if (nbt == null) return;

        final NonNullList<ItemStack> keys = NonNullList.withSize(nbt.getTagList("Items", 10).tagCount(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(nbt, keys);

        for (ItemStack stack : keys) {
            if (stack != ItemStack.EMPTY) {
                instance.add(stack);
            }
        }
    }
}

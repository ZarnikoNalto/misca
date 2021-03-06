package msifeed.misca.charsheet.cap;

import msifeed.misca.charsheet.ICharsheet;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class CharsheetStorage implements Capability.IStorage<ICharsheet> {
    @Nullable
    @Override
    public NBTBase writeNBT(Capability<ICharsheet> capability, ICharsheet instance, EnumFacing side) {
        final NBTTagCompound nbt = new NBTTagCompound();
        nbt.setBoolean("IsPlayer", true);
        nbt.setString(Tag.name, instance.getName());
        nbt.setString(Tag.wikiPage, instance.getWikiPage());
        instance.skills().writeNBT(Tag.skills, nbt);
        instance.effortPools().writeNBT(Tag.effortPools, nbt);
        instance.resources().writeNBT(Tag.resources, nbt);

        return nbt;
    }

    @Override
    public void readNBT(Capability<ICharsheet> capability, ICharsheet instance, EnumFacing side, NBTBase nbtBase) {
        final NBTTagCompound nbt = (NBTTagCompound) nbtBase;
        instance.setName(nbt.getString(Tag.name));
        instance.setWikiPage(nbt.getString(Tag.wikiPage));
        instance.skills().readNBT(nbt, Tag.skills);
        instance.effortPools().readNBT(nbt, Tag.effortPools);
        instance.resources().readNBT(nbt, Tag.resources);
    }

    private static class Tag {
        private static final String name = "name";
        private static final String wikiPage = "wiki";
        private static final String skills = "skills";
        private static final String effortPools = "effortPools";
        private static final String resources = "resources";
    }
}

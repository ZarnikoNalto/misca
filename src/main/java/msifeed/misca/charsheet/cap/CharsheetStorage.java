package msifeed.misca.charsheet.cap;

import msifeed.misca.charsheet.ICharsheet;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.Map;

public class CharsheetStorage implements Capability.IStorage<ICharsheet> {

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<ICharsheet> capability, ICharsheet instance, EnumFacing side) {
        final NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString(Tag.name, instance.getName());
        nbt.setString(Tag.wikiPage, instance.getWikiPage());
        instance.skills().writeNBT(Tag.skills, nbt);

        final NBTTagCompound potionsNbt = new NBTTagCompound();
        for (Map.Entry<Potion, Integer> entry : instance.potions().entrySet())
            potionsNbt.setByte(entry.getKey().getRegistryName().toString(), entry.getValue().byteValue());
        nbt.setTag(Tag.potions, potionsNbt);

        final NBTTagCompound enchantsNbt = new NBTTagCompound();
        for (Map.Entry<Enchantment, Integer> entry : instance.enchants().entrySet())
            enchantsNbt.setByte(entry.getKey().getRegistryName().toString(), entry.getValue().byteValue());
        nbt.setTag(Tag.enchants, enchantsNbt);

        return nbt;
    }

    @Override
    public void readNBT(Capability<ICharsheet> capability, ICharsheet instance, EnumFacing side, NBTBase nbtBase) {
        final NBTTagCompound nbt = (NBTTagCompound) nbtBase;
        instance.setName(nbt.getString(Tag.name));
        instance.setWikiPage(nbt.getString(Tag.wikiPage));
        instance.skills().readNBT(nbt, Tag.skills);

        instance.potions().clear();
        final NBTTagCompound potionsNbt = nbt.getCompoundTag(Tag.potions);
        for (String key : potionsNbt.getKeySet()) {
            final Potion p = Potion.getPotionFromResourceLocation(key);
            if (p != null) {
                instance.potions().put(p, (int) potionsNbt.getByte(key));
            }
        }

        instance.enchants().clear();
        final NBTTagCompound enchantsNbt = nbt.getCompoundTag(Tag.enchants);
        for (String key : enchantsNbt.getKeySet()) {
            final Enchantment e = Enchantment.getEnchantmentByLocation(key);
            if (e != null) {
                instance.enchants().put(e, (int) enchantsNbt.getByte(key));
            }
        }
    }

    private static class Tag {
        private static final String name = "name";
        private static final String wikiPage = "wiki";
        private static final String skills = "skills";
        private static final String effortPools = "effortPools";
        private static final String resources = "Resources";
        private static final String needsRest = "needsRest";
        private static final String needsLost = "needsLost";
        private static final String potions = "potions";
        private static final String enchants = "enchants";
        private static final String updated = "updated";
    }
}

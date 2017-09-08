package ru.ariadna.misca.things;

import com.google.common.collect.Ordering;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ru.ariadna.misca.utils.AlphanumComparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class MiscaCreativeTab extends CreativeTabs {
    private ArrayList<ItemStack> cache = new ArrayList<>();

    public MiscaCreativeTab() {
        super("misca");
    }

    @Override
    public Item getTabIconItem() {
        return Items.bowl;
    }

    @Override
    public boolean hasSearchBar() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public void displayAllReleventItems(List list) {
        cache.clear();
        for (Object anItemRegistry : Item.itemRegistry) {
            Item item = (Item) anItemRegistry;
            if (item == null) continue;

            for (CreativeTabs tab : item.getCreativeTabs()) {
                if (tab == this) {
                    item.getSubItems(item, this, cache);
                }
            }
        }

        cache.sort(Comparator.comparing(ItemStack::getUnlocalizedName, new AlphanumComparator()));
        list.addAll(cache);

        if (this.func_111225_m() != null)
        {
            this.addEnchantmentBooksToList(list, this.func_111225_m());
        }
    }
}

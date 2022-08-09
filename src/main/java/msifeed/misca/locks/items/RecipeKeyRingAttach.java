package msifeed.misca.locks.items;

import msifeed.misca.Misca;
import msifeed.misca.locks.LocksConfig;
import msifeed.misca.locks.cap.keyring.IKeyRing;
import msifeed.misca.locks.cap.keyring.KeyRingProvider;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.LinkedList;
import java.util.List;

public class RecipeKeyRingAttach extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    private final Item keyRing;
    private final Item key;

    public RecipeKeyRingAttach(Item keyRing, Item key) {
        this.keyRing = keyRing;
        this.key = key;
    }

    @Override
    public boolean matches(InventoryCrafting crafting, World world) {
        return doLookup(crafting) != null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting crafting) {
        final Lookup l = doLookup(crafting);
        if (l == null)
            return null;
        final ItemStack s = l.keyRing.copy();
        final IKeyRing keyRing = KeyRingProvider.get(s);
        if (keyRing == null)
            return null;
        for (ItemStack key : l.keys) {
            final ItemStack keyCopy = key.copy();
            keyCopy.setCount(1);
            keyRing.add(keyCopy);
        }
        return s;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    private Lookup doLookup(InventoryCrafting crafting) {
        final Lookup lookup = new Lookup();

        for (int i = 0; i < crafting.getSizeInventory(); ++i) {
            final ItemStack stackInSlot = crafting.getStackInSlot(i);
            if (stackInSlot.isEmpty()) continue;

            final Item itemInSlot = stackInSlot.getItem();
            if (itemInSlot != key && itemInSlot != keyRing) return null;

            if (itemInSlot == key && !ItemKey.isBlank(stackInSlot)) {
                lookup.keys.add(stackInSlot);
            } else if (itemInSlot == keyRing && lookup.keyRing == null) {
                lookup.keyRing = stackInSlot;
            } else {
                return null;
            }
        }

        if (lookup.keyRing == null || lookup.keys.isEmpty())
            return null;

        final IKeyRing keyRing = KeyRingProvider.get(lookup.keyRing);
        final LocksConfig config = Misca.getSharedConfig().locks;

        if (keyRing != null && keyRing.getKeys().size() + lookup.keys.size() > config.keyRingMaxKeys)
            return null;

        return lookup;
    }

    private static class Lookup {
        ItemStack keyRing;
        List<ItemStack> keys = new LinkedList<>();
    }
}

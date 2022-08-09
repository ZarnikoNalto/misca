package msifeed.misca.locks.items;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RecipeLockCloning extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    private final Item lock;

    public RecipeLockCloning(Item lock) {
        this.lock = lock;
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
        final ItemStack s = l.lock.copy();
        s.setCount(l.blanks + 1);
        return s;
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
            if (itemInSlot != lock) return null;

            if (ItemLock.isBlank(stackInSlot)) {
                lookup.blanks += 1;
            } else if (lookup.lock == null) {
                lookup.lock = stackInSlot;
            } else {
                return null; // Can't duplicate two keys at once
            }
        }

        if (lookup.lock == null || lookup.blanks == 0)
            return null;

        return lookup;
    }

    private static class Lookup {
        ItemStack lock;
        int blanks;
    }
}

package msifeed.misca.locks.items;

import msifeed.misca.locks.cap.key.LockKeyProvider;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RecipeLockSetting extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    private final Item key;
    private final Item lock;

    public RecipeLockSetting(Item key, Item lock) {
        this.key = key;
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
        final ItemStack s = ItemLock.createLock(lock, LockKeyProvider.get(l.key).getSecret());
        s.setCount(l.blankLocks);
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

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        final NonNullList<ItemStack> remainingItems = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        final Lookup l = doLookup(inv);
        if (l == null)
            return null;
        remainingItems.set(l.keySlot, ForgeHooks.getContainerItem(l.key));
        return remainingItems;
    }

    private Lookup doLookup(InventoryCrafting crafting) {
        final Lookup lookup = new Lookup();

        for (int i = 0; i < crafting.getSizeInventory(); ++i) {
            final ItemStack stackInSlot = crafting.getStackInSlot(i);
            if (stackInSlot.isEmpty()) continue;

            final Item itemInSlot = stackInSlot.getItem();
            if (itemInSlot != key && itemInSlot != lock) return null;

            if (itemInSlot == key && !ItemKey.isBlank(stackInSlot) && lookup.key == null) {
                lookup.key = stackInSlot;
                lookup.keySlot = i;
            } else if (itemInSlot == lock && ItemLock.isBlank(stackInSlot)) {
                lookup.blankLocks += 1;
            } else {
                return null;
            }
        }

        if (lookup.key == null || lookup.blankLocks == 0)
            return null;

        return lookup;
    }

    private static class Lookup {
        ItemStack key;
        int keySlot;
        int blankLocks;
    }
}

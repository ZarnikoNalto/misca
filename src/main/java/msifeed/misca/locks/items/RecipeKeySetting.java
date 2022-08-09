package msifeed.misca.locks.items;

import msifeed.misca.locks.cap.key.LockKeyProvider;
import msifeed.misca.locks.cap.lock.LockProvider;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RecipeKeySetting extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    private final Item key;
    private final Item lock;

    public RecipeKeySetting(Item key, Item lock) {
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
        final ItemStack s = ItemKey.createKey(LockProvider.get(l.lock).getSecret());
        s.setCount(l.blankKeys);
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
        remainingItems.set(l.lockSlot, ForgeHooks.getContainerItem(l.lock));
        return remainingItems;
    }

    private Lookup doLookup(InventoryCrafting crafting) {
        final Lookup lookup = new Lookup();

        for (int i = 0; i < crafting.getSizeInventory(); ++i) {
            final ItemStack stackInSlot = crafting.getStackInSlot(i);
            if (stackInSlot.isEmpty()) continue;

            final Item itemInSlot = stackInSlot.getItem();
            if (itemInSlot != key && itemInSlot != lock) return null;

            if (itemInSlot == lock && !ItemLock.isBlank(stackInSlot) && lookup.lock == null) {
                lookup.lock = stackInSlot;
                lookup.lockSlot = i;
            } else if (itemInSlot == key && ItemKey.isBlank(stackInSlot)) {
                lookup.blankKeys += 1;
            } else {
                return null;
            }
        }

        if (lookup.lock == null || lookup.blankKeys == 0)
            return null;

        return lookup;
    }

    private static class Lookup {
        ItemStack lock;
        int lockSlot;
        int blankKeys;
    }
}

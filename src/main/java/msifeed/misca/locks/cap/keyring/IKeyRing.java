package msifeed.misca.locks.cap.keyring;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface IKeyRing {
    void add(ItemStack stack);

    boolean remove(ItemStack stack);

    boolean checkSecret(int secret);

    void updateSecrets();

    NonNullList<ItemStack> getKeys();
}

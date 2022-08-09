package msifeed.misca.locks.cap.keyring;

import msifeed.misca.locks.cap.key.ILockKey;
import msifeed.misca.locks.cap.key.LockKeyProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.HashMap;
import java.util.Stack;

public class KeyRingImpl implements IKeyRing {
    private final NonNullList<ItemStack> keys = NonNullList.create();
    private final HashMap<Integer, Boolean> secrets = new HashMap<>();

    @Override
    public void add(ItemStack stack) {
        keys.add(stack);

        final ILockKey key = LockKeyProvider.get(stack);
        if (key != null) {
            secrets.put(key.getSecret(), true);
        }
    }

    @Override
    public boolean remove(ItemStack stack) {
        final ILockKey key = LockKeyProvider.get(stack);
        if (key != null) {
            secrets.remove(key.getSecret());
        }

        return keys.remove(stack);
    }

    @Override
    public boolean checkSecret(int secret) {
        return secrets.containsKey(secret) && secrets.get(secret);
    }

    @Override
    public void updateSecrets() {
        for (ItemStack stack : keys) {
            final ILockKey key = LockKeyProvider.get(stack);
            if (key == null)
                continue;

            secrets.put(key.getSecret(), true);
        }
    }

    @Override
    public NonNullList<ItemStack> getKeys() {
        return keys;
    }
}

package msifeed.misca.locks;

import msifeed.misca.Misca;
import msifeed.misca.locks.items.*;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LockItems {
    public static final Item lockDigital = new ItemLock(LockType.digital);
    public static final Item lockMechanical = new ItemLock(LockType.mechanical);
    public static final Item lockMagical = new ItemLock(LockType.magical);
//    public static final Item lockDigital = new ItemLock(LockType.digital);

    public static final Item pickMechanical = new ItemPick(LockType.mechanical);
    public static final Item pickMagical = new ItemPick(LockType.magical);

    public static final Item key = new ItemKey().setRegistryName(Misca.MODID, ItemKey.ID);
    public static final Item skeletalKey = new ItemSkeletalKey();
    public static final Item keyRing = new ItemKeyRing();

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                lockMechanical,
                lockMagical,
                lockDigital,
                pickMechanical,
                pickMagical,
                key,
                skeletalKey,
                keyRing
        );
    }

    @SubscribeEvent
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        event.getRegistry().registerAll(
                new RecipeKeyCloning(key).setRegistryName(Misca.MODID, "clone_mechanical_key"),
                new RecipeLockSetting(key, lockDigital).setRegistryName(Misca.MODID, "setting_digital_lock"),
                new RecipeLockSetting(key, lockMechanical).setRegistryName(Misca.MODID, "setting_mechanical_lock"),
                new RecipeLockSetting(key, lockMagical).setRegistryName(Misca.MODID, "setting_magical_lock"),
                new RecipeKeyCloning(lockMechanical).setRegistryName(Misca.MODID, "clone_mechanical_lock"),
                new RecipeKeyCloning(lockMagical).setRegistryName(Misca.MODID, "clone_magical_lock"),
                new RecipeKeyRingAttach(keyRing, key).setRegistryName(Misca.MODID, "key_ring_attach"),
                new RecipeKeySetting(key, lockMechanical).setRegistryName(Misca.MODID, "setting_mechanical_key"),
                new RecipeKeySetting(key, lockMagical).setRegistryName(Misca.MODID, "setting_magical_key")
        );
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomMeshDefinition(lockDigital, stack -> LockModels.DIGITAL_LOC_MODEL);
        ModelBakery.registerItemVariants(lockDigital, LockModels.DIGITAL_LOC_MODEL);
        ModelLoader.setCustomMeshDefinition(lockMechanical, stack -> LockModels.MECH_LOC_MODEL);
        ModelBakery.registerItemVariants(lockMechanical, LockModels.MECH_LOC_MODEL);
        ModelLoader.setCustomMeshDefinition(lockMagical, stack -> LockModels.MAGI_LOC_MODEL);
        ModelBakery.registerItemVariants(lockMagical, LockModels.MAGI_LOC_MODEL);

        ModelLoader.setCustomModelResourceLocation(pickMechanical, 0, LockModels.getModel(pickMechanical.getRegistryName()));
        ModelLoader.setCustomModelResourceLocation(pickMagical, 0, LockModels.getModel(pickMagical.getRegistryName()));

        ModelLoader.setCustomModelResourceLocation(key, 0, LockModels.getModel(new ResourceLocation(Misca.MODID, "blank_key")));
        ModelLoader.setCustomModelResourceLocation(key, 1, LockModels.getModel(key.getRegistryName()));
        ModelLoader.setCustomModelResourceLocation(skeletalKey, 0, LockModels.getModel(skeletalKey.getRegistryName()));
        ModelLoader.setCustomModelResourceLocation(keyRing, 0, LockModels.getModel(keyRing.getRegistryName()));
    }
}

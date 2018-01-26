package msifeed.mc.misca.crabs.tools;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import msifeed.mc.misca.things.MiscaThings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Arrays;
import java.util.List;

public class ItemHealthController extends Item {
    public ItemHealthController() {
        setUnlocalizedName("health_controller");
        setTextureName("nether_star");
        setCreativeTab(MiscaThings.itemsTab);
        setMaxStackSize(1);
    }

    public void changeHealth(EntityLivingBase entity, float amount) {
        final float currentEntityHealth = entity.getHealth();
        if (currentEntityHealth > 1.0F && currentEntityHealth <= entity.getMaxHealth())
            entity.setHealth(entity.getHealth() + amount);
    }

    @Override
    public boolean hasEffect(ItemStack par1ItemStack, int pass) {
        return true;
    }

    @Override
    public int getEntityLifespan(ItemStack itemStack, World world) {
        return 0;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        String desc = LanguageRegistry.instance().getStringLocalization("item.health_controller.desc");
        desc = StringEscapeUtils.unescapeJava(desc);
        lines.addAll(Arrays.asList(desc.split("\n")));
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase player, ItemStack stack) {
        // Shift+ЛКМ - добавить 1 хп себе

        if (player.isSneaking()) {
            this.changeHealth(player, 1.0F);
        }

        return true;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity target) {
        // ЛКМ по существу - добавить 1 хп

        if (!player.isSneaking()) {
            if (FMLCommonHandler.instance().getSide().isClient() || !(target instanceof EntityLivingBase)) return true;
            final EntityLivingBase entity = (EntityLivingBase) target;
            this.changeHealth(entity, 1.0F);
        }

        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        // Shift+ПКМ - убрать 1 хп себе

        if (player.isSneaking()) {
            this.changeHealth(player, -1.0F);
        }

        return stack;
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onEntityInteract(EntityInteractEvent event) {
        // ПКМ по существу: убрать 1 хп

        if (event.entityPlayer.isSneaking()) {
            if (!(event.target instanceof EntityLivingBase)) return;
            final EntityLivingBase entity = (EntityLivingBase) event.target;
            this.changeHealth(entity, -1.0F);
        }
    }
}

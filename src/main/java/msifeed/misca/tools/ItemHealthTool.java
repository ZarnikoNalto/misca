package msifeed.misca.tools;

import msifeed.misca.Misca;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemHealthTool extends Item {
    public static final String ID = "health_tool";

    public ItemHealthTool() {
        setRegistryName(Misca.MODID, ID);
        setTranslationKey(ID);
        setCreativeTab(CreativeTabs.TOOLS);
        setMaxStackSize(1);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        if (!player.world.isRemote && !player.isSneaking() && entity instanceof EntityLivingBase) {
            changeHealth((EntityLivingBase) entity, false);
        }
        return true;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity, EnumHand hand) {
        if (!player.world.isRemote && !player.isSneaking()) {
            changeHealth(entity, true);
        }
        return true;
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase player, ItemStack stack) {
        if (player.isSneaking()) {
            changeHealth(player, false);
        }
        return true;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote && player.isSneaking()) {
            changeHealth(player, true);
            return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
        }
        return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    private void changeHealth(EntityLivingBase entity, boolean isPositive) {
        final float value = isPositive ? 1.0f : -1.0f;
        final float currentEntityHealth = entity.getHealth();
        final float result = currentEntityHealth + value;
        if (result > 0 && result <= entity.getMaxHealth())
            entity.setHealth(result);
    }
}

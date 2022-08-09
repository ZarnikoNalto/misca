package msifeed.misca.charsheet.cap;

import msifeed.misca.Misca;
import msifeed.misca.charsheet.*;
import msifeed.misca.chatex.ChatexRpc;
import msifeed.misca.keeper.KeeperSync;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class CharsheetEventHandler {
    public static final String KNOCKED_OUT = "misca.knocked_out";
    public static final String LAST_DAMAGE = "misca.last_damage";
    public static final String DAMAGE_THRESHOLD = "misca.damage_threshold";

    @SubscribeEvent
    public void onAttachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityLivingBase) {
            final AbstractAttributeMap attributes = ((EntityLivingBase) event.getObject()).getAttributeMap();
            attributes.registerAttribute(CharSkill.MOD);

            if (event.getObject() instanceof EntityPlayer) {
                event.addCapability(ICharsheet.KEY, new CharsheetProvider());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.player.world.isRemote) {
            KeeperSync.INSTANCE.sync((EntityPlayerMP) event.player);
            event.player.refreshDisplayName();
        };

        AttributesFlow.updateAttributes(event.player);
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        AttributesFlow.updateAttributes(event.player);
    }

    @SubscribeEvent
    public void onPlayerSpawn(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) return;
        if (!(event.getEntity() instanceof EntityPlayerMP)) return;

        final EntityPlayerMP player = (EntityPlayerMP) event.getEntity();
        KeeperSync.INSTANCE.sync(player);
        CharsheetSync.sync(player, player);
        player.refreshDisplayName();
    }

    @SubscribeEvent
    public void onPlayerTracking(net.minecraftforge.event.entity.player.PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof EntityPlayer) {
            CharsheetSync.sync((EntityPlayerMP) event.getEntityPlayer(), (EntityPlayer) event.getTarget());
        }
    }

    @SubscribeEvent
    public void onPlayerClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
        final ICharsheet original = CharsheetProvider.get(event.getOriginal());
        final ICharsheet cloned = CharsheetProvider.get(event.getEntityPlayer());
        cloned.replaceWith(original);
        CharsheetSync.sync(event.getEntityPlayer());
    }

    @SubscribeEvent
    public void onNameUpdate(net.minecraftforge.event.entity.player.PlayerEvent.NameFormat event) {
        final ICharsheet cs = CharsheetProvider.get(event.getEntityPlayer());
        if (!cs.getName().isEmpty())
            event.setDisplayname(cs.getName());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityDamaged(LivingDamageEvent event) {
        if (event.isCanceled()) return;

        if (event.getSource().getTrueSource() instanceof EntityPlayer) {
            final EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
            final long curTime = System.currentTimeMillis();
            final CharsheetConfig config = Misca.getSharedConfig().charsheet;

            if (player.inventory.getCurrentItem() == ItemStack.EMPTY) {
                event.setAmount((float) (event.getAmount() + config.fitnessFistsDamageOffset + CharSkill.fitness.get(player) * config.fitnessFistsDamageFactor));
            }

            if (event.getEntity() instanceof EntityPlayer) {
                final EntityPlayer target = (EntityPlayer) event.getEntity();
                final float health = target.getHealth();

                if (curTime - target.getEntityData().getLong(LAST_DAMAGE) >= config.damageThresholdDelay) {
                    target.getEntityData().setLong(LAST_DAMAGE, curTime);
                    target.getEntityData().setFloat(DAMAGE_THRESHOLD, 0);
                }

                final float healthThreshold = target.getMaxHealth() * config.damageThresholdFactor;
                final float threshold = target.getEntityData().getFloat(DAMAGE_THRESHOLD);
                final float remainDamage = Math.min(Math.max(event.getAmount() - threshold, 0.0f), healthThreshold);

                target.getEntityData().setFloat(DAMAGE_THRESHOLD, threshold + event.getAmount());

                if (curTime - target.getEntityData().getLong(LAST_DAMAGE) <= config.damageThresholdDelay
                        && target.getEntityData().getFloat(DAMAGE_THRESHOLD) > healthThreshold) {
                    event.setAmount(remainDamage);
                }

                if (health <= event.getAmount() && !target.getEntityData().getBoolean(KNOCKED_OUT)) {
                    target.getEntityData().setBoolean(KNOCKED_OUT, true);
                    ChatexRpc.broadcastEvent((EntityPlayerMP) target, "misca.event.knockout");
                }

                if (target.getEntityData().getBoolean(KNOCKED_OUT)) {
                    event.setAmount(health - 0.01f);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityHealed(LivingHealEvent event) {
        if (event.isCanceled()) return;

        if (event.getEntity() instanceof EntityPlayer) {
            final EntityPlayer player = (EntityPlayer) event.getEntity();

            if (player.getEntityData().getBoolean(KNOCKED_OUT)) {
                player.getEntityData().setBoolean(KNOCKED_OUT, false);
                ChatexRpc.broadcastEvent((EntityPlayerMP) player, "misca.event.getup");
            }
        }
    }
}

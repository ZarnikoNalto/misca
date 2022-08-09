package msifeed.misca.charstate;

import com.google.gson.reflect.TypeToken;
import msifeed.misca.Misca;
import msifeed.misca.charsheet.CharNeed;
import msifeed.misca.charsheet.CharSkill;
import msifeed.misca.charstate.cap.*;
import msifeed.misca.charstate.client.CharstateHudHandler;
import msifeed.misca.charstate.handler.*;
import msifeed.misca.chatex.ChatexConfig;
import msifeed.misca.chatex.SpeechEvent;
import msifeed.misca.combat.battle.Battle;
import msifeed.misca.combat.battle.BattleManager;
import msifeed.misca.combat.cap.CombatantProvider;
import msifeed.misca.combat.cap.ICombatant;
import msifeed.misca.regions.RegionControl;
import msifeed.misca.stages.StageCharacter;
import msifeed.sys.cap.FloatContainer;
import msifeed.sys.sync.SyncChannel;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public enum Charstate {
    INSTANCE;

    private static final int UPDATE_INTERVAL_SEC = 2;

    private final IntegrityHandler integrityHandler = new IntegrityHandler();
    private final SanityHandler sanityHandler = new SanityHandler();
    private final StaminaHandler staminaHandler = new StaminaHandler();
    private final EffectsHandler effectsHandler = new EffectsHandler();

    public static class ItemEffectsConfig extends HashMap<ResourceLocation, ItemEffectInfo[]> {
    }

    public static final SyncChannel<ItemEffectsConfig> ITEM_EFFECTS
            = new SyncChannel<>(Misca.RPC, "item_effects.json", TypeToken.get(ItemEffectsConfig.class));

    public static HashMap<ResourceLocation, ItemEffectInfo[]> getItemEffects() {
        return ITEM_EFFECTS.get();
    }

    public void preInit() {
        CapabilityManager.INSTANCE.register(ICharstate.class, new CharstateStorage(), CharstateImpl::new);
        MinecraftForge.EVENT_BUS.register(new CharstateEventHandler());
        MinecraftForge.EVENT_BUS.register(this);
        Misca.RPC.register(new CharstateSync());

        if (FMLCommonHandler.instance().getSide().isClient()) {
            MinecraftForge.EVENT_BUS.register(new CharstateHudHandler());
        }
    }

    public static void sync() throws Exception {
        ITEM_EFFECTS.sync();
    }

    @SubscribeEvent
    public void onPlayerCreate(EntityEvent.EntityConstructing event) {
        if (!(event.getEntity() instanceof EntityPlayer)) return;

        final AbstractAttributeMap attributes = ((EntityLivingBase) event.getEntity()).getAttributeMap();
        attributes.registerAttribute(IntegrityHandler.INTEGRITY);
        attributes.registerAttribute(SanityHandler.SANITY);
        attributes.registerAttribute(StaminaHandler.STAMINA);
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        final AbstractAttributeMap src = event.getOriginal().getAttributeMap();
        final AbstractAttributeMap dst = event.getEntityPlayer().getAttributeMap();
        copyAttribute(src, dst, IntegrityHandler.INTEGRITY);
        copyAttribute(src, dst, SanityHandler.SANITY);
        copyAttribute(src, dst, StaminaHandler.STAMINA);
    }

    private static void copyAttribute(AbstractAttributeMap src, AbstractAttributeMap dst, IAttribute attr) {
        final double base = src.getAttributeInstance(attr).getBaseValue();
        dst.getAttributeInstance(attr).setBaseValue(base);
//        final ModifiableAttributeInstance instance = (ModifiableAttributeInstance) dst.getAttributeInstance(attr);
//        for (AttributeModifier mod : src.getAttributeInstance(attr).getModifiers()) {
//            instance.applyModifier(mod);
//        }
    }

    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntity() instanceof EntityPlayerMP)) return;
        if (event.getEntity().world.isRemote) return;

        final EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();
        if (player.isCreative()) return;

        final ICharstate state = CharstateProvider.get(player);
        final long passedSec = state.passedFromUpdate();
        if (passedSec < UPDATE_INTERVAL_SEC) return;

        final Map<CharNeed, Double> effects = RegionControl.getLocalEffects(player);
        integrityHandler.handleTime(player, passedSec, effects.getOrDefault(CharNeed.INT, 0d));
        sanityHandler.handleTime(player, UPDATE_INTERVAL_SEC, effects.getOrDefault(CharNeed.SAN, 0d));
        staminaHandler.handleTime(player, passedSec, effects.getOrDefault(CharNeed.STA, 0d));

        tickTolerances(player, state.tolerances(), passedSec);

        state.resetUpdateTime();
        CharstateSync.sync(player);
    }

    private static void tickTolerances(EntityPlayerMP player, FloatContainer<CharNeed> tolerances, long passedSec) {
        final CharstateConfig config = Misca.getSharedConfig().charstate;

        final double regionMod = RegionControl.getLocalToleranceMod(player);

        for (CharNeed need : CharNeed.values()) {
            final float val = tolerances.get(need);
            if (val <= 0) continue;

            final double factor = Math.max(0, 1 + regionMod);
            final double lost = config.getToleranceLost(val) * passedSec * factor;
            tolerances.set(need, (float) (val - lost));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerDamaged(LivingDamageEvent event) {
        if (event.isCanceled() || event.getAmount() < 1) return;
        if (event.getEntity().world.isRemote) return;
        if (!(event.getEntity() instanceof EntityPlayer)) return;

        final EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (player.isCreative()) return;

        final ICombatant com = CombatantProvider.get(player);
        if (com.isInBattle()) {
            final Battle battle = BattleManager.getBattle(com.getBattleId());
            if (battle != null && battle.isTraining())
                return;
        }

        final float amount = Math.min(player.getHealth(), event.getAmount());

        integrityHandler.handleDamage(player, amount);
        sanityHandler.handleDamage(player, amount);
    }

    @SubscribeEvent
    public void onSpeech(SpeechEvent event) {
        final CharstateConfig config = Misca.getSharedConfig().charstate;
        final ChatexConfig chat = Misca.getSharedConfig().chat;

        final EntityPlayerMP source = event.getPlayer();
        if (source.isCreative()) return;

        final int range = chat.getSpeechRange(event.getMessage());
        final double threshold = range * chat.garble.thresholdPart;
        final int chars = event.getMessage().length();

        final int charisma = CharSkill.charisma.get(source);
        final double chaSanityMod = config.charismaSanityGainFactor * charisma;
        final double chaStaminaMod = config.charismaStaminaGainFactor * charisma;

        for (EntityPlayer listener : source.world.playerEntities) {
            if (listener == source) continue;
            if (listener.isCreative()) continue;

            final float distance = listener.getDistance(source);
            if (distance > range) continue;

            final Map<CharNeed, Double> regionEffects = RegionControl.getLocalEffects(event.getPlayer());
            final double distanceMod = distance > threshold
                    ? -(distance - threshold) / range
                    : 0;

            sanityHandler.handleSpeech(listener, chars, distanceMod + chaSanityMod, regionEffects.getOrDefault(CharNeed.SAN, 0d));
            staminaHandler.handleSpeech(listener, chars, distanceMod + chaStaminaMod, regionEffects.getOrDefault(CharNeed.STA, 0d));
        }
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getEntityPlayer().isCreative()) return;
        if (event.getEntityPlayer().getHeldItemMainhand().isEmpty()) return;

        staminaHandler.handleMining(event);
    }

    @SubscribeEvent
    public void onItemCrafted(net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent event) {
        if (event.player.isCreative()) return;

        staminaHandler.handleCrafting(event);
    }

    public void onFoodEaten(EntityPlayer player, ItemStack stack) {
        if (player.world.isRemote) return;
        if (player.isCreative()) return;

        sanityHandler.handleItemUse(player, stack);
        effectsHandler.handleItemUse(player, stack);
    }
}

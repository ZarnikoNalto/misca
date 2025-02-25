package msifeed.misca;

import com.google.gson.reflect.TypeToken;
import msifeed.misca.books.CommandExternalBook;
import msifeed.misca.charsheet.cap.CharsheetProvider;
import msifeed.misca.charstate.Charstate;
import msifeed.misca.chatex.Chatex;
import msifeed.misca.client.MiscaClient;
import msifeed.misca.cmd.*;
import msifeed.misca.combat.Combat;
import msifeed.misca.environ.Environ;
import msifeed.misca.environ.EnvironCommand;
import msifeed.misca.keeper.KeeperSync;
import msifeed.misca.locks.Locks;
import msifeed.misca.logdb.LogDB;
import msifeed.misca.potions.CombatPotions;
import msifeed.misca.potions.DrugPotion;
import msifeed.misca.potions.NeedsPotions;
import msifeed.misca.potions.OtherPotions;
import msifeed.misca.regions.CommandRegions;
import msifeed.misca.regions.RegionControl;
import msifeed.misca.rename.RenameItems;
import msifeed.misca.rolls.RollRpc;
import msifeed.misca.stages.Stages;
import msifeed.misca.supplies.BackgroundSupplies;
import msifeed.misca.supplies.InvoiceCommand;
import msifeed.misca.tags.ItemTagsCommand;
import msifeed.misca.tags.Tags;
import msifeed.misca.tweaks.DisableSomeDamageTypes;
import msifeed.misca.tweaks.MiscaCrashInfo;
import msifeed.sys.rpc.RpcChannel;
import msifeed.sys.sync.SyncChannel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = Misca.MODID, name = Misca.NAME)
public class Misca {
    public static final String MODID = "misca";
    public static final String NAME = "Misca";

    public static final RpcChannel RPC = new RpcChannel(MODID + ":rpc");
    public static final SyncChannel<MiscaSharedConfig> SHARED
            = new SyncChannel<>(RPC, "shared.json", TypeToken.get(MiscaSharedConfig.class));

    private final Chatex chatex = new Chatex();
    private final Combat combat = new Combat();
    private final Environ environ = new Environ();
    private final Locks locks = new Locks();
    private final BackgroundSupplies supplies = new BackgroundSupplies();

    public static MiscaSharedConfig getSharedConfig() {
        return SHARED.get();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().registerCrashCallable(new MiscaCrashInfo());

        MiscaThings.init();
        CharsheetProvider.preInit();
        combat.preInit();
        locks.preInit();
        supplies.preInit();
        Charstate.INSTANCE.preInit();
        RegionControl.preInit();
        Stages.init();

        MinecraftForge.EVENT_BUS.register(new NeedsPotions());
        MinecraftForge.EVENT_BUS.register(new CombatPotions());
        MinecraftForge.EVENT_BUS.register(OtherPotions.class);
        MinecraftForge.EVENT_BUS.register(new DisableSomeDamageTypes());

        Misca.RPC.register(new RollRpc());

        if (FMLCommonHandler.instance().getSide().isClient()) {
            MiscaClient.INSTANCE.preInit();
            MinecraftForge.EVENT_BUS.register(DrugPotion.class);
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        ConfigManager.sync(MODID, Config.Type.INSTANCE);

        chatex.init();
        combat.init();
        environ.init();

        RenameItems.register();
        Tags.init();
        RegionControl.init();

        if (FMLCommonHandler.instance().getSide().isClient())
            MiscaClient.INSTANCE.init();
    }

    public static void syncConfig() throws Exception {
        ConfigManager.sync(Misca.MODID, Config.Type.INSTANCE);

        SHARED.sync();
        Combat.sync();
        Charstate.sync();
        LogDB.reload();
        KeeperSync.reload();
        RegionControl.sync();
        Stages.sync();
        Tags.sync();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        try {
            syncConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }

        chatex.registerCommands(event);
        event.registerServerCommand(new MiscaCommand());
        event.registerServerCommand(new RollCommand());
        event.registerServerCommand(new RenameCommand());
        event.registerServerCommand(new EnvironCommand());
        event.registerServerCommand(new CombatCommand());
        event.registerServerCommand(new LocksCommand());
        event.registerServerCommand(new InvoiceCommand());
        event.registerServerCommand(new NeedsCommand());
        event.registerServerCommand(new SkillsCommand());
        event.registerServerCommand(new BlessCommand());
        event.registerServerCommand(new UnstuckCommand());
        event.registerServerCommand(new CommandRegions());
        event.registerServerCommand(new CharsheetCommand());
        event.registerServerCommand(new CommandExternalBook());
        event.registerServerCommand(new ItemTagsCommand());
    }

    @SubscribeEvent
    public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(MODID))
            ConfigManager.sync(MODID, Config.Type.INSTANCE);
    }
}

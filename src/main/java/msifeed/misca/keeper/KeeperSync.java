package msifeed.misca.keeper;

import com.google.gson.Gson;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import msifeed.misca.MiscaConfig;
import msifeed.misca.charsheet.*;
import msifeed.misca.charsheet.cap.CharsheetProvider;
import msifeed.misca.combat.CharAttribute;
import net.minecraft.entity.player.EntityPlayerMP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.codecs.ValueCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.concurrent.TimeUnit;

public enum KeeperSync {
    INSTANCE;

    static final Logger LOG = LogManager.getLogger("Misca-Keeper");
    private MongoCollection<KeeperCharsheet> sheets;

    public static void reload() {
        final KeeperConfig cfg = MiscaConfig.keeper;
        if (cfg.disabled) return;

        final String conn = String.format("mongodb://%s:%s@%s:%d/%s?authSource=%s",
                cfg.username, cfg.password,
                cfg.host, cfg.port,
                cfg.database, cfg.authDatabase);

        try {
            LOG.info("Try to connect to Keeper DB...");

            final MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(conn))
                    .applyToConnectionPoolSettings(builder -> builder.maxWaitTime(5, TimeUnit.SECONDS))
                    .codecRegistry(CodecRegistries.fromProviders(
                            PojoCodecProvider.builder()
                                    .register(KeeperCharsheet.class)
                                    .build(),
                            new ValueCodecProvider()
                    ))
                    .build();
            INSTANCE.sheets = MongoClients.create(settings)
                    .getDatabase(cfg.database)
                    .getCollection(cfg.collection, KeeperCharsheet.class);
            LOG.info("Connection to Keeper DB is successful");
        } catch (Exception e) {
            LOG.error("Failed to connect to Keeper DB", e);
        }
    }

    public void sync(EntityPlayerMP player) {
        if (sheets == null) return;

        final String username = player.getGameProfile().getName();
        LOG.info("Sync {} with Keeper DB...", username);

        try {
            final KeeperCharsheet sheet = sheets.find(Filters.eq("character", username)).first();
            if (sheet == null) {
                LOG.warn("Player entry is not found");
                return;
            }

            LOG.info("Found entry: " + new Gson().toJson(sheet));

            final ICharsheet cs = CharsheetProvider.get(player);

            for (CharSkill key : CharSkill.values())
                cs.skills().set(key, sheet.skills.getOrDefault(key.name(), 0));
            for (CharCraft key : CharCraft.values())
                cs.crafts().set(key, sheet.crafts.getOrDefault(key.name(), 0));
        } catch (Exception e) {
            LOG.error("Failed to get info from Keeper", e);
        }
    }
}

package o7410.bundlesbeyond;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public class BundlesBeyondConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("bundles_beyond.json");
    private static final Gson GSON = new GsonBuilder()
            //? if >=1.21.4
            /*.setStrictness(Strictness.STRICT)*/
            .setPrettyPrinting()
            .create();

    private static BundlesBeyondConfig instance = new BundlesBeyondConfig();

    public ScrollMode scrollMode = ScrollMode.VANILLA;
    public ModEnabledState modEnabledState = ModEnabledState.ON;
    public int slotSize = 24;

    public static BundlesBeyondConfig instance() {
        return instance;
    }

    public static boolean save() {
        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
            BundlesBeyondClient.LOGGER.info("Writing config to file");
            GSON.toJson(toJson(), writer);
            return true;
        } catch (IOException | JsonIOException | SecurityException e) {
            BundlesBeyondClient.LOGGER.error("Error writing config to file: {}", e.toString());
            return false;
        }
    }

    @SuppressWarnings("LoggingSimilarMessage")
    public static boolean load() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                BundlesBeyondClient.LOGGER.info("Creating config file in '{}'", CONFIG_PATH);
                save();
                return true;
            }
            BundlesBeyondClient.LOGGER.info("Reading config from file");
            try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
                JsonObject rawConfig;
                try {
                    rawConfig = GSON.fromJson(reader, JsonObject.class);
                } catch (JsonSyntaxException e) {
                    BundlesBeyondClient.LOGGER.error("Error reading config from file: {}", e.toString());
                    save();
                    return false;
                }
                if (rawConfig.has("scrollAxisKeybindMode")) {
                    BundlesBeyondClient.LOGGER.info("Migrating old config");
                    migrateOldConfig(rawConfig);
                }
                BundlesBeyondConfig config = fromJson(rawConfig);
                if (config == null) {
                    return false;
                }
                instance = config;
                return true;
            } catch (IOException | JsonIOException e) {
                BundlesBeyondClient.LOGGER.error("Error reading config from file: {}", e.toString());
                return false;
            }
        } catch (SecurityException e) {
            BundlesBeyondClient.LOGGER.error("Error reading config from file: {}", e.toString());
            return false;
        }
    }

    private static final Codec<Integer> BUNDLE_SLOT_SIZE_CODEC = Codec.INT.comapFlatMap(
            integer -> integer >= 18 && integer <= 24 ?
                    DataResult.success(integer) :
                    DataResult.error(() -> "slotSize must be between 18 and 24, found " + integer),
            Function.identity()
    );
    private static final Codec<BundlesBeyondConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ScrollMode.CODEC.fieldOf("scrollMode").forGetter(config -> config.scrollMode),
            ModEnabledState.CODEC.fieldOf("modEnabledState").forGetter(config -> config.modEnabledState),
            BUNDLE_SLOT_SIZE_CODEC.fieldOf("slotSize").forGetter(config -> config.slotSize)
    ).apply(instance, (scrollMode, modEnabledState, slotSize) -> {
        BundlesBeyondConfig config = new BundlesBeyondConfig();
        config.scrollMode = scrollMode;
        config.modEnabledState = modEnabledState;
        config.slotSize = slotSize;
        return config;
    }));

    private static BundlesBeyondConfig fromJson(JsonObject jsonObject) {
        return CODEC.parse(JsonOps.INSTANCE, jsonObject).resultOrPartial(BundlesBeyondClient.LOGGER::error).orElse(null);
    }

    private static JsonObject toJson() {
        return CODEC.encodeStart(JsonOps.INSTANCE, instance).getOrThrow().getAsJsonObject();
    }

    private static void migrateOldConfig(JsonObject config) {
        if (config.has("scrollAxisKeybindMode")) {
            String oldScrollMode = config.get("scrollAxisKeybindMode").getAsString();

            if ("toggle".equals(oldScrollMode)) {
                boolean isHorizontal = config.has("scrollingToggledHorizontal")
                        && config.get("scrollingToggledHorizontal").getAsBoolean();

                config.addProperty("scrollMode", isHorizontal ? "horizontal" : "vertical");
            }

            config.remove("scrollAxisKeybindMode");
            config.remove("scrollingToggledHorizontal");
        }

        if (config.has("modEnabledKeyModeOnToggle") && !config.get("modEnabledKeyModeOnToggle").getAsBoolean()) {
            config.addProperty("modEnabledState", "hold_key");
        } else {
            boolean isOn = config.has("modEnabledWhenOnToggle")
                    && config.get("modEnabledWhenOnToggle").getAsBoolean();

            config.addProperty("modEnabledState", isOn ? "on" : "off");
        }

        config.remove("modEnabledKeyModeOnToggle");
        config.remove("modEnabledWhenOnToggle");
    }

}

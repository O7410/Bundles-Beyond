package o7410.bundlesbeyond;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class BundlesBeyondConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("bundles_beyond.json");
    private static final Gson GSON = new GsonBuilder()
//            .setStrictness(Strictness.STRICT) // 1.21.8
            .setPrettyPrinting()
            .create();

    private static BundlesBeyondConfig instance = new BundlesBeyondConfig();

    public ScrollMode scrollMode = ScrollMode.VANILLA;
    public ModEnabledState modEnabledState = ModEnabledState.ON;

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
                fromJson(rawConfig);
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

    private static void fromJson(JsonObject jsonObject) {
        boolean resave = false;

        Optional<ScrollMode> scrollModeResult = ScrollMode.CODEC.decode(
                JsonOps.INSTANCE, jsonObject.remove("scrollMode")
        ).map(Pair::getFirst).result();
        if (scrollModeResult.isPresent()) {
            instance.scrollMode = scrollModeResult.get();
        } else {
            resave = true;
        }

        Optional<ModEnabledState> modEnabledStateResult = ModEnabledState.CODEC.decode(
                JsonOps.INSTANCE, jsonObject.remove("modEnabledState")
        ).map(Pair::getFirst).result();
        if (modEnabledStateResult.isPresent()) {
            instance.modEnabledState = modEnabledStateResult.get();
        } else {
            resave = true;
        }

        resave = resave || !jsonObject.entrySet().isEmpty();
        if (resave) {
            save();
        }
    }

    private static JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("scrollMode", instance.scrollMode.toString());
        jsonObject.addProperty("modEnabledState", instance.modEnabledState.toString());
        return jsonObject;
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

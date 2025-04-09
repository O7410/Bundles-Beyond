package o7410.bundlesbeyond;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JavaOps;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.io.IOException;

public class BundlesBeyondConfig {
    public static ConfigClassHandler<BundlesBeyondConfig> HANDLER = ConfigClassHandler.createBuilder(BundlesBeyondConfig.class)
            .id(Identifier.of(BundlesBeyondClient.MOD_ID, "bundles_beyond"))
            .serializer(handler -> GsonConfigSerializerBuilder.create(handler)
                    .appendGsonBuilder(gsonBuilder -> gsonBuilder.registerTypeAdapter(ScrollAxisKeybindMode.class, new TypeAdapter<ScrollAxisKeybindMode>() {
                        @Override
                        public void write(JsonWriter out, ScrollAxisKeybindMode value) throws IOException {
                            out.value(value.toString());
                        }

                        @Override
                        public ScrollAxisKeybindMode read(JsonReader in) throws IOException {
                            String value = in.nextString();
                            return ScrollAxisKeybindMode.CODEC.decode(JavaOps.INSTANCE, value)
                                    .mapOrElse(Pair::getFirst, pairError -> handler.instance().scrollAxisKeybindMode);
                        }
                    }))
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("bundles_beyond.json")).build())
            .build();

    public static BundlesBeyondConfig instance() {
        return HANDLER.instance();
    }

    @SerialEntry public ScrollAxisKeybindMode scrollAxisKeybindMode = ScrollAxisKeybindMode.VANILLA;
    @SerialEntry public boolean modEnabledKeyModeOnToggle = true;
    @SerialEntry public boolean modEnabledWhenOnToggle = true;
    @SerialEntry public boolean scrollingToggledHorizontal = true;
}

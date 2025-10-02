package o7410.bundlesbeyond;

import com.mojang.serialization.Codec;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

import java.util.function.Supplier;

public enum ModEnabledState implements StringIdentifiable {
    ON("On, press key to toggle", "on", "On", () -> true),
    OFF("Off, press key to toggle", "off", "Off", () -> false),
    HOLD_KEY("Hold key to enable", "hold_key", "Hold key", () -> InputUtil.isKeyPressed(
            MinecraftClient.getInstance().getWindow()/*? if <1.21.9 {*/.getHandle()/*?}*/,
            BundlesBeyond.getKeyCode(BundlesBeyond.MOD_ENABLED_KEY)
    ));

    public static final Codec<ModEnabledState> CODEC = StringIdentifiable.createCodec(ModEnabledState::values);

    public final String description;
    public final String id;
    public final String shortName;
    private final Supplier<Boolean> isEnabled;

    ModEnabledState(String description, String id, String shortName, Supplier<Boolean> isEnabled) {
        this.id = id;
        this.description = description;
        this.shortName = shortName;
        this.isEnabled = isEnabled;
    }

    public boolean isEnabled() {
        return isEnabled.get();
    }

    public Text getDescriptionText() {
        return Text.literal(this.description);
    }

    public Text getShortNameText() {
        return Text.literal(this.shortName).styled(style -> style.withHoverEvent(
                //? if <1.21.8 {
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, this.getDescriptionText())
                //?} else {
                /*new HoverEvent.ShowText(this.getDescriptionText())
                 *///?}
        ));
    }

    @Override
    public String asString() {
        return this.id;
    }

    @Override
    public String toString() {
        return this.id;
    }
}

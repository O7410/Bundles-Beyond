package o7410.bundlesbeyond;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.util.StringRepresentable;

public enum ModEnabledState implements StringRepresentable {
    ON("On, press key to toggle", "on", "On", () -> true),
    OFF("Off, press key to toggle", "off", "Off", () -> false),
    HOLD_KEY("Hold key to enable", "hold_key", "Hold key", () -> InputConstants.isKeyDown(
            Minecraft.getInstance().getWindow()/*? if <1.21.10 {*//*.getWindow()*//*?}*/,
            BundlesBeyond.getKeyCode(BundlesBeyond.MOD_ENABLED_KEY)
    ));

    public static final Codec<ModEnabledState> CODEC = StringRepresentable.fromEnum(ModEnabledState::values);

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

    public Component getDescriptionComponent() {
        return Component.literal(this.description);
    }

    public Component getShortNameComponent() {
        return Component.literal(this.shortName).withStyle(style -> style.withHoverEvent(
                //? if <1.21.8 {
                /*new HoverEvent(HoverEvent.Action.SHOW_TEXT, this.getDescriptionComponent())
                *///?} else {
                new HoverEvent.ShowText(this.getDescriptionComponent())
                 //?}
        ));
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    @Override
    public String toString() {
        return this.id;
    }
}

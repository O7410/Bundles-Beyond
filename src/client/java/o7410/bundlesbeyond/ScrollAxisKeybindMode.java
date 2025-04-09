package o7410.bundlesbeyond;

import com.mojang.serialization.Codec;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

public enum ScrollAxisKeybindMode implements StringIdentifiable {
    HOLD_FOR_VERTICAL("Hold key to scroll vertically", "hold_for_vertical", "Hold for vertical"),
    HOLD_FOR_HORIZONTAL("Hold key to scroll horizontally", "hold_for_horizontal", "Hold for horizontal"),
    TOGGLE("Press key to toggle between scrolling horizontally and vertically", "toggle", "Toggle"),
    VANILLA("Vanilla scrolling", "vanilla", "Vanilla");

    public static final Codec<ScrollAxisKeybindMode> CODEC = StringIdentifiable.createCodec(ScrollAxisKeybindMode::values);

    public final String description;
    public final String id;
    public final String shortName;

    ScrollAxisKeybindMode(String description, String id, String shortName) {
        this.id = id;
        this.description = description;
        this.shortName = shortName;
    }

    public Text getDescriptionText() {
        return Text.literal(this.description);
    }

    public Text getShortNameText() {
        return Text.literal(this.shortName);
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

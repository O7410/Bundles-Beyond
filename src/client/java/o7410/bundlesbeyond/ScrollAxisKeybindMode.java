package o7410.bundlesbeyond;

import com.mojang.serialization.Codec;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

public enum ScrollAxisKeybindMode implements StringIdentifiable {
    HOLD_FOR_VERTICAL("Hold key to scroll vertically", "hold_for_vertical"),
    HOLD_FOR_HORIZONTAL("Hold key to scroll horizontally", "hold_for_horizontal"),
    TOGGLE("Press key to toggle between scrolling horizontally and vertically", "toggle"),
    VANILLA("Vanilla scrolling", "vanilla");

    public static final Codec<ScrollAxisKeybindMode> CODEC = StringIdentifiable.createCodec(ScrollAxisKeybindMode::values);

    private final String description;
    private final String shortName;

    ScrollAxisKeybindMode(String description, String shortName) {
        this.shortName = shortName;
        this.description = description;
    }

    public Text getDescription() {
        return Text.literal(this.description);
    }

    @Override
    public String asString() {
        return this.shortName;
    }

    @Override
    public String toString() {
        return this.shortName;
    }
}

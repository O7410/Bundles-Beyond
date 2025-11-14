package o7410.bundlesbeyond;

import com.mojang.serialization.Codec;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

public enum ScrollMode implements StringIdentifiable {
    HOLD_FOR_VERTICAL("Hold key to scroll vertically", "hold_for_vertical", "Hold for vertical"),
    HOLD_FOR_HORIZONTAL("Hold key to scroll horizontally", "hold_for_horizontal", "Hold for horizontal"),
    HORIZONTAL("Horizontal, press key for vertical", "horizontal", "Horizontal"),
    VERTICAL("Vertical, press key for horizontal", "vertical", "Vertical"),
    VANILLA("Vanilla scrolling", "vanilla", "Vanilla");

    public static final Codec<ScrollMode> CODEC = StringIdentifiable.createCodec(ScrollMode::values);

    public final String description;
    public final String id;
    public final String shortName;

    ScrollMode(String description, String id, String shortName) {
        this.id = id;
        this.description = description;
        this.shortName = shortName;
    }

    public Text getDescriptionText() {
        return Text.literal(this.description);
    }

    public Text getShortNameText() {
        return Text.literal(this.shortName).styled(style -> style.withHoverEvent(
                //? if <1.21.8 {
                /*new HoverEvent(HoverEvent.Action.SHOW_TEXT, this.getDescriptionText())
                *///?} else {
                new HoverEvent.ShowText(this.getDescriptionText())
                //?}
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

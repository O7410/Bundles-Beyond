package o7410.bundlesbeyond;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.util.StringRepresentable;

public enum ScrollMode implements StringRepresentable {
    HOLD_FOR_VERTICAL("Hold key to scroll vertically", "hold_for_vertical", "Hold for vertical"),
    HOLD_FOR_HORIZONTAL("Hold key to scroll horizontally", "hold_for_horizontal", "Hold for horizontal"),
    HORIZONTAL("Horizontal, press key for vertical", "horizontal", "Horizontal"),
    VERTICAL("Vertical, press key for horizontal", "vertical", "Vertical"),
    VANILLA("Vanilla scrolling", "vanilla", "Vanilla");

    public static final Codec<ScrollMode> CODEC = StringRepresentable.fromEnum(ScrollMode::values);

    public final String description;
    public final String id;
    public final String shortName;

    ScrollMode(String description, String id, String shortName) {
        this.id = id;
        this.description = description;
        this.shortName = shortName;
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

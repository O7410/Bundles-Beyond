package o7410.bundlesbeyond.mixin;

import net.minecraft.client.gui.widget.SliderWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SliderWidget.class)
public interface SliderWidgetAccessor {
    @Accessor
    boolean getSliderFocused();

    @Accessor
    void setSliderFocused(boolean sliderFocused);
}

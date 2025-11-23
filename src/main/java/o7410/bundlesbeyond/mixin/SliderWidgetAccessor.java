package o7410.bundlesbeyond.mixin;

import net.minecraft.client.gui.components.AbstractSliderButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractSliderButton.class)
public interface SliderWidgetAccessor {
    @Accessor
    boolean getCanChangeValue();

    @Accessor
    void setCanChangeValue(boolean sliderFocused);
}

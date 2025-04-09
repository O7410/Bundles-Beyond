package o7410.bundlesbeyond.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.BundleTooltipSubmenuHandler;
import net.minecraft.client.util.InputUtil;
import o7410.bundlesbeyond.BundlesBeyondConfig;
import o7410.bundlesbeyond.ScrollAxisKeybindMode;
import o7410.bundlesbeyond.BundleTooltipAdditions;
import o7410.bundlesbeyond.BundlesBeyondClient;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BundleTooltipSubmenuHandler.class)
public abstract class BundleTooltipSubmenuHandlerMixin {

    @Shadow @Final private MinecraftClient client;

    @ModifyExpressionValue(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/Scroller;scrollCycling(DII)I"))
    private int changeScrollBehavior(int original, @Local Vector2i scrollOffset, @Local(ordinal = 1) int size, @Local(ordinal = 2) int scrollAmount, @Local(ordinal = 3) int selectedIndex) {
        BundlesBeyondConfig config = BundlesBeyondConfig.instance();
        if (!BundlesBeyondClient.isModEnabled() || config.scrollAxisKeybindMode == ScrollAxisKeybindMode.VANILLA) return original;
        int width = BundleTooltipAdditions.getModifiedBundleTooltipColumns(size);
        int height = BundleTooltipAdditions.getModifiedBundleTooltipRows(size, width);
        boolean keyPressed = InputUtil.isKeyPressed(this.client.getWindow().getHandle(), ((KeyBindingAccessor) BundlesBeyondClient.scrollAxisKey).getBoundKey().getCode());
        boolean isVertical = switch (config.scrollAxisKeybindMode) {
            case HOLD_FOR_VERTICAL -> keyPressed;
            case HOLD_FOR_HORIZONTAL -> !keyPressed;
            case TOGGLE -> !config.scrollingToggledHorizontal;
            default -> throw new IllegalStateException();
        };
        if (isVertical) {
            return BundleTooltipAdditions.offsetVertical(size, width, height, selectedIndex, -scrollAmount);
        }
        if (scrollOffset.y == 0) {
            return BundleTooltipAdditions.offsetHorizontal(size, width, height, selectedIndex, -scrollAmount);
        }
        return original;
    }
}

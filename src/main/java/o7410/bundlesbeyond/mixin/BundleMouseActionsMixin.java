package o7410.bundlesbeyond.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.BundleMouseActions;
import o7410.bundlesbeyond.BundlesBeyondConfig;
import o7410.bundlesbeyond.ScrollMode;
import o7410.bundlesbeyond.BundleTooltipAdditions;
import o7410.bundlesbeyond.BundlesBeyond;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BundleMouseActions.class)
public abstract class BundleMouseActionsMixin {

    @Shadow @Final private Minecraft minecraft;

    @ModifyExpressionValue(method = "onMouseScrolled", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ScrollWheelHandler;getNextScrollWheelSelection(DII)I"))
    private int changeScrollBehavior(int original, @Local Vector2i scrollOffset, @Local(ordinal = 1) int size, @Local(ordinal = 2) int scrollAmount, @Local(ordinal = 3) int selectedIndex) {
        BundlesBeyondConfig config = BundlesBeyondConfig.instance();
        if (!BundlesBeyond.isModEnabled() || config.scrollMode == ScrollMode.VANILLA || size <= 4) return original;
        int width = BundleTooltipAdditions.getModifiedBundleTooltipColumns(size);
        int height = BundleTooltipAdditions.getModifiedBundleTooltipRows(size, width);
        boolean keyPressed = InputConstants.isKeyDown(this.minecraft.getWindow()/*? if <1.21.10 {*//*.getWindow()*//*?}*/, BundlesBeyond.getKeyCode(BundlesBeyond.SCROLL_AXIS_KEY));
        boolean isVertical = switch (config.scrollMode) {
            case HOLD_FOR_VERTICAL -> keyPressed;
            case HOLD_FOR_HORIZONTAL -> !keyPressed;
            case VERTICAL -> true;
            case HORIZONTAL -> false;
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

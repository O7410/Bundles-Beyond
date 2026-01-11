package o7410.bundlesbeyond.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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

    @WrapOperation(method = "onMouseScrolled", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ScrollWheelHandler;getNextScrollWheelSelection(DII)I"))
    private int changeScrollBehavior(double scrollAmountDouble, int selectedIndex, int size, Operation<Integer> original, @Local Vector2i scrollOffset, @Local(ordinal = 2) int scrollAmount) {
        BundlesBeyondConfig config = BundlesBeyondConfig.instance();
        if (!BundlesBeyond.isModEnabled()) {
            return original.call(scrollAmountDouble, selectedIndex, size);
        }
        if (config.reverseView) scrollAmountDouble = -scrollAmountDouble;
        if (config.scrollMode == ScrollMode.VANILLA || size <= 4) {
            return original.call(scrollAmountDouble, selectedIndex, size);
        }

        int width = BundleTooltipAdditions.getModifiedBundleTooltipColumns(size);
        int height = BundleTooltipAdditions.getModifiedBundleTooltipRows(size, width);
        boolean keyPressed = !BundlesBeyond.SCROLL_AXIS_KEY.isUnbound() && InputConstants.isKeyDown(
                this.minecraft.getWindow()/*? if <1.21.10 {*//*.getWindow()*//*?}*/,
                BundlesBeyond.getKeyCode(BundlesBeyond.SCROLL_AXIS_KEY)
        );
        boolean isVertical = switch (config.scrollMode) {
            case HOLD_FOR_VERTICAL -> keyPressed;
            case HOLD_FOR_HORIZONTAL -> !keyPressed;
            case VERTICAL -> true;
            case HORIZONTAL -> false;
            default -> throw new IllegalStateException();
        };
        if (scrollOffset.y == 0) {
            return BundleTooltipAdditions.offsetHorizontal(size, width, height, selectedIndex, -scrollAmount);
        }
        if (isVertical) {
            return BundleTooltipAdditions.offsetVertical(size, width, height, selectedIndex, scrollAmount);
        }
        return original.call(scrollAmountDouble, selectedIndex, size);
    }
}

package o7410.bundlesbeyond.mixin;

import net.minecraft.client.gui.tooltip.BundleTooltipComponent;
import net.minecraft.component.type.BundleContentsComponent;
import o7410.bundlesbeyond.BundleTooltipAdditions;
import o7410.bundlesbeyond.BundlesBeyondClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleTooltipComponent.class)
public abstract class BundleTooltipComponentMixin {
    @Shadow @Final private BundleContentsComponent bundleContents;

    @Inject(method = "getNumVisibleSlots", at = @At("HEAD"), cancellable = true)
    private void changeNumberOfVisibleSlots(CallbackInfoReturnable<Integer> cir) {
        if (!BundlesBeyondClient.isModEnabled()) return;
        cir.setReturnValue(this.bundleContents.size());
    }

    @ModifyVariable(method = "drawNonEmptyTooltip", at = @At("STORE"))
    private boolean changeIfToDrawExtraItemsCount(boolean original) {
        if (!BundlesBeyondClient.isModEnabled()) return original;
        return false;
    }

    @ModifyConstant(
            method = "drawNonEmptyTooltip",
            constant = @Constant(intValue = 4),
            slice = @Slice(
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/tooltip/BundleTooltipComponent;drawSelectedItemTooltip(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/client/gui/DrawContext;III)V"
                    )
            )
    )
    private int modifyDrawnColumnsCount(int constant) {
        if (!BundlesBeyondClient.isModEnabled()) return constant;
        return BundleTooltipAdditions.getModifiedBundleTooltipColumns(this.bundleContents.size());
    }

    @ModifyConstant(method = "drawNonEmptyTooltip", constant = @Constant(intValue = 96))
    private int modifyRightAlignmentForItems(int constant) {
        if (!BundlesBeyondClient.isModEnabled()) return constant;
        return 24 * BundleTooltipAdditions.getModifiedBundleTooltipColumns(this.bundleContents.size());
    }

    @ModifyConstant(method = "getXMargin", constant = @Constant(intValue = 96))
    private int modfiyXMargin(int constant) {
        if (!BundlesBeyondClient.isModEnabled()) return constant;
        return 24 * BundleTooltipAdditions.getModifiedBundleTooltipColumns(this.bundleContents.size());
    }

    @ModifyConstant(method = "getWidth", constant = @Constant(intValue = 96))
    private int changeTooltipWidth(int constant) {
        if (!BundlesBeyondClient.isModEnabled()) return constant;
        return 24 * BundleTooltipAdditions.getModifiedBundleTooltipColumns(this.bundleContents.size());
    }

    @ModifyConstant(method = "getProgressBarFill", constant = @Constant(intValue = 94))
    private int changeProgressBarFill(int constant) {
        if (!BundlesBeyondClient.isModEnabled()) return constant;
        return 24 * BundleTooltipAdditions.getModifiedBundleTooltipColumns(this.bundleContents.size()) - 2;
    }

    @ModifyConstant(method = "getRows", constant = @Constant(intValue = 4))
    private int modifyItemsPerRow(int constant) {
        if (!BundlesBeyondClient.isModEnabled()) return constant;
        return BundleTooltipAdditions.getModifiedBundleTooltipColumns(this.bundleContents.size());
    }

    @ModifyConstant(method = "drawProgressBar", constant = @Constant(intValue = 96))
    private int changeProgressBorderWidth(int constant) {
        if (!BundlesBeyondClient.isModEnabled()) return constant;
        return 24 * BundleTooltipAdditions.getModifiedBundleTooltipColumns(this.bundleContents.size());
    }

    @ModifyConstant(method = "drawProgressBar", constant = @Constant(intValue = 48))
    private int changeTextCenterHorizontalOffset(int constant) {
        if (!BundlesBeyondClient.isModEnabled()) return constant;
        return 12 * BundleTooltipAdditions.getModifiedBundleTooltipColumns(this.bundleContents.size());
    }
}

package o7410.bundlesbeyond.mixin;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.BundleTooltipComponent;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleTooltipComponent.class)
public abstract class BundleTooltipComponentMixin {
    @Shadow @Final private BundleContentsComponent bundleContents;

    @Unique
    private int getModifiedColumnCount() {
        return Math.max(4, MathHelper.ceil(Math.sqrt(this.bundleContents.size())));
    }

    @Inject(method = "getNumVisibleSlots", at = @At("HEAD"), cancellable = true)
    private void changeNumberOfVisibleSlots(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(this.bundleContents.size());
    }

    @ModifyVariable(method = "drawNonEmptyTooltip", at = @At("STORE"))
    private boolean changeIfToDrawExtraItemsCount(boolean original) {
        return false;
    }

    @ModifyConstant(
            method = "drawNonEmptyTooltip",
            constant = @Constant(intValue = 4),
            slice = @Slice(
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/tooltip/BundleTooltipComponent;drawSelectedItemTooltip(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/client/gui/DrawContext;II)V"
                    )
            )
    )
    private int modifyDrawnColumnsCount(int constant) {
        return this.getModifiedColumnCount();
    }

    @ModifyConstant(method = "drawNonEmptyTooltip", constant = @Constant(intValue = 96))
    private int modifyRightAlignmentForItems(int constant) {
        return 24 * this.getModifiedColumnCount();
    }

    @Inject(method = "getWidth", at = @At("HEAD"), cancellable = true)
    private void changeTooltipWidth(TextRenderer textRenderer, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(24 * this.getModifiedColumnCount());
    }

    @ModifyConstant(method = "getProgressBarFill", constant = @Constant(intValue = 94))
    private int changeProgressBarFill(int constant) {
        return 24 * this.getModifiedColumnCount() - 2;
    }

    @ModifyConstant(method = "getRows", constant = @Constant(intValue = 4))
    private int modifyItemsPerRow(int constant) {
        return this.getModifiedColumnCount();
    }

    @ModifyConstant(method = "drawProgressBar", constant = @Constant(intValue = 96))
    private int changeProgressBorderWidth(int constant) {
        return 24 * this.getModifiedColumnCount();
    }

    @ModifyConstant(method = "drawProgressBar", constant = @Constant(intValue = 48))
    private int changeTextCenterHorizontalOffset(int constant) {
        return 12 * this.getModifiedColumnCount();
    }
}

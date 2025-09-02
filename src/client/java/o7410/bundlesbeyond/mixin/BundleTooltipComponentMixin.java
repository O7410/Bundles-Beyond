package o7410.bundlesbeyond.mixin;

//import com.mojang.blaze3d.pipeline.RenderPipeline; // 1.21.8
//import net.minecraft.client.render.RenderLayer; // 1.21.4
//import java.util.function.Function; // 1.21.4
//import net.minecraft.util.Identifier; // 1.21.4+
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.BundleTooltipComponent;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.text.Text;
import o7410.bundlesbeyond.BundleTooltipAdditions;
import o7410.bundlesbeyond.BundlesBeyondClient;
import o7410.bundlesbeyond.BundlesBeyondConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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
        return BundleTooltipAdditions.getModifiedBundleTooltipColumnsPixels(this.bundleContents.size());
    }

    @ModifyConstant(method = "drawNonEmptyTooltip", constant = @Constant(intValue = 24))
    private int modifySlotSize(int constant) {
        if (!BundlesBeyondClient.isModEnabled()) return constant;
        return BundlesBeyondConfig.instance().slotSize;
    }

    @ModifyConstant(method = "getXMargin", constant = @Constant(intValue = 96))
    private int modfiyXMargin(int constant) {
        if (!BundlesBeyondClient.isModEnabled() || this.bundleContents.isEmpty()) return constant;
        return BundleTooltipAdditions.getModifiedBundleTooltipColumnsPixels(this.bundleContents.size());
    }

    @ModifyConstant(method = "getWidth", constant = @Constant(intValue = 96))
    private int changeTooltipWidth(int constant) {
        if (!BundlesBeyondClient.isModEnabled() || this.bundleContents.isEmpty()) return constant;
        return BundleTooltipAdditions.getModifiedBundleTooltipColumnsPixels(this.bundleContents.size());
    }

    @ModifyConstant(method = "getProgressBarFill", constant = @Constant(intValue = 94))
    private int changeProgressBarFill(int constant) {
        if (!BundlesBeyondClient.isModEnabled() || this.bundleContents.isEmpty()) return constant;
        return BundleTooltipAdditions.getModifiedBundleTooltipColumnsPixels(this.bundleContents.size()) - 2;
    }

    @ModifyConstant(method = "getRows", constant = @Constant(intValue = 4))
    private int modifyItemsPerRow(int constant) {
        if (!BundlesBeyondClient.isModEnabled()) return constant;
        return BundleTooltipAdditions.getModifiedBundleTooltipColumns(this.bundleContents.size());
    }

    @ModifyConstant(method = "drawProgressBar", constant = @Constant(intValue = 96))
    private int changeProgressBorderWidth(int constant) {
        if (!BundlesBeyondClient.isModEnabled() || this.bundleContents.isEmpty()) return constant;
        return BundleTooltipAdditions.getModifiedBundleTooltipColumnsPixels(this.bundleContents.size());
    }

    @ModifyConstant(method = "drawProgressBar", constant = @Constant(intValue = 48))
    private int removeVanillaProgressBarTextOffset(int constant) {
        if (!BundlesBeyondClient.isModEnabled() || this.bundleContents.isEmpty()) return constant;
        return 0;
    }

    @WrapOperation(method = "drawProgressBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawCenteredTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V"))
    private void fractionalOffsetProgressBarText(DrawContext instance, TextRenderer textRenderer, Text text, int centerX, int y, int color, Operation<Void> original) {
        if (!BundlesBeyondClient.isModEnabled() || this.bundleContents.isEmpty()) {
            original.call(instance, textRenderer, text, centerX, y, color);
            return;
        }
//        instance.getMatrices().pushMatrix(); // 1.21.8
        instance.getMatrices().push(); // 1.21.3
        float offset = BundleTooltipAdditions.getModifiedBundleTooltipColumnsPixels(this.bundleContents.size()) / 2f;
//        instance.getMatrices().translate(offset, 0); // 1.21.8
        instance.getMatrices().translate(offset, 0, 0); // 1.21.3
        original.call(instance, textRenderer, text, centerX, y, color);
//        instance.getMatrices().popMatrix(); // 1.21.8
        instance.getMatrices().pop(); // 1.21.3
    }

    @ModifyConstant(method = "getRowsHeight", constant = @Constant(intValue = 24))
    private int changeRowHeight(int constant) {
        if (!BundlesBeyondClient.isModEnabled()) return constant;
        return BundlesBeyondConfig.instance().slotSize;
    }
    // 1.21.3
    @ModifyConstant(method = "drawItem", constant = @Constant(intValue = 24))
    private int modifySlotTextureSize(int constant) {
        if (!BundlesBeyondClient.isModEnabled()) return constant;
        return BundlesBeyondConfig.instance().slotSize;
    }

    /* // 1.21.8
    @WrapOperation(method = "drawItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V"))
    private void fractionalSlotOffset(DrawContext instance, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, Operation<Void> original) { */
    /* // 1.21.4
    @WrapOperation(method = "drawItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V"))
    private void fractionalSlotOffset(DrawContext instance, Function<Identifier, RenderLayer> renderLayers, Identifier sprite, int x, int y, int width, int height, Operation<Void> original) {
        if (!BundlesBeyondClient.isModEnabled()) {
//            original.call(instance, pipeline, sprite, x, y, width, height); // 1.21.8
            original.call(instance, renderLayers, sprite, x, y, width, height); // 1.21.4
            return;
        }
//        instance.getMatrices().pushMatrix(); // 1.21.8
        instance.getMatrices().push(); // 1.21.4
        float offset = (BundlesBeyondConfig.instance().slotSize - 24) / 2f;
//        instance.getMatrices().translate(offset, offset); // 1.21.8
        instance.getMatrices().translate(offset, offset, 0); // 1.21.4
//        original.call(instance, pipeline, sprite, x, y, width, height); // 1.21.8
        original.call(instance, renderLayers, sprite, x, y, width, height); // 1.21.4
//        instance.getMatrices().popMatrix(); // 1.21.8
        instance.getMatrices().pop(); // 1.21.4
    }*/

    @Inject(method = "drawItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawItem(Lnet/minecraft/item/ItemStack;III)V"))
    private void fractionalItemOffset(CallbackInfo ci, @Local(argsOnly = true) DrawContext drawContext) {
        if (!BundlesBeyondClient.isModEnabled()) return;
//        drawContext.getMatrices().pushMatrix(); // 1.21.8
        drawContext.getMatrices().push(); // 1.21.3
        float offset = (BundlesBeyondConfig.instance().slotSize - 16) / 2f;
//        drawContext.getMatrices().translate(offset, offset); // 1.21.8
        drawContext.getMatrices().translate(offset, offset, 0); // 1.21.3
    }

    @ModifyConstant(method = "drawItem", constant = @Constant(intValue = 4))
    private int removeVanillaItemOffset(int constant) {
        if (!BundlesBeyondClient.isModEnabled()) return constant;
        return 0;
    }

    @Inject(method = "drawItem", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawStackOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;II)V",
            shift = At.Shift.AFTER))
    private void resetItemOffset(CallbackInfo ci, @Local(argsOnly = true) DrawContext drawContext) {
        if (!BundlesBeyondClient.isModEnabled()) return;
//        drawContext.getMatrices().popMatrix(); // 1.21.8
        drawContext.getMatrices().pop(); // 1.21.3
    }
}

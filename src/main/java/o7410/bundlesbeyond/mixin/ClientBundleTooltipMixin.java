package o7410.bundlesbeyond.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
//? if >=1.21.8
import com.mojang.blaze3d.pipeline.RenderPipeline;
//? if =1.21.4 {
/*import net.minecraft.client.renderer.RenderType;
import java.util.function.Function;
*///?}
//? if >=1.21.4
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.BundleContents;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import o7410.bundlesbeyond.BundleTooltipAdditions;
import o7410.bundlesbeyond.BundlesBeyond;
import o7410.bundlesbeyond.BundlesBeyondConfig;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientBundleTooltip.class)
public abstract class ClientBundleTooltipMixin {
    @Shadow @Final private BundleContents contents;

    @Inject(method = "slotCount", at = @At("HEAD"), cancellable = true)
    private void changeNumberOfVisibleSlots(CallbackInfoReturnable<Integer> cir) {
        if (!BundlesBeyond.isModEnabled()) return;
        cir.setReturnValue(this.contents.size());
    }

    @ModifyVariable(method = "renderBundleWithItemsTooltip", at = @At("STORE"))
    private boolean changeIfToDrawExtraItemsCount(boolean original) {
        if (!BundlesBeyond.isModEnabled()) return original;
        return false;
    }

    @ModifyConstant(
            method = "renderBundleWithItemsTooltip",
            constant = @Constant(intValue = 4),
            slice = @Slice(
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientBundleTooltip;drawSelectedItemTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/client/gui/GuiGraphics;III)V"
                    )
            )
    )
    private int modifyDrawnColumnsCount(int constant) {
        if (!BundlesBeyond.isModEnabled()) return constant;
        return BundleTooltipAdditions.getModifiedBundleTooltipColumns(this.contents.size());
    }

    @ModifyConstant(method = "renderBundleWithItemsTooltip", constant = @Constant(intValue = 96))
    private int modifyRightAlignmentForItems(int constant) {
        if (!BundlesBeyond.isModEnabled()) return constant;
        return BundleTooltipAdditions.getModifiedBundleTooltipColumnsPixels(this.contents.size());
    }

    @ModifyConstant(method = "renderBundleWithItemsTooltip", constant = @Constant(intValue = 24))
    private int modifySlotSize(int constant) {
        if (!BundlesBeyond.isModEnabled()) return constant;
        return BundlesBeyondConfig.instance().slotSize;
    }

    @ModifyConstant(method = "getContentXOffset", constant = @Constant(intValue = 96))
    private int modfiyXMargin(int constant) {
        if (!BundlesBeyond.isModEnabled() || this.contents.isEmpty()) return constant;
        return BundleTooltipAdditions.getModifiedBundleTooltipColumnsPixels(this.contents.size());
    }

    @ModifyConstant(method = "getWidth", constant = @Constant(intValue = 96))
    private int changeTooltipWidth(int constant) {
        if (!BundlesBeyond.isModEnabled() || this.contents.isEmpty()) return constant;
        return BundleTooltipAdditions.getModifiedBundleTooltipColumnsPixels(this.contents.size());
    }

    @ModifyConstant(method = "getProgressBarFill", constant = @Constant(intValue = 94))
    private int changeProgressBarFill(int constant) {
        if (!BundlesBeyond.isModEnabled() || this.contents.isEmpty()) return constant;
        return BundleTooltipAdditions.getModifiedBundleTooltipColumnsPixels(this.contents.size()) - 2;
    }

    @ModifyConstant(method = "gridSizeY", constant = @Constant(intValue = 4))
    private int modifyItemsPerRow(int constant) {
        if (!BundlesBeyond.isModEnabled()) return constant;
        return BundleTooltipAdditions.getModifiedBundleTooltipColumns(this.contents.size());
    }

    @ModifyConstant(method = "drawProgressbar", constant = @Constant(intValue = 96))
    private int changeProgressBorderWidth(int constant) {
        if (!BundlesBeyond.isModEnabled() || this.contents.isEmpty()) return constant;
        return BundleTooltipAdditions.getModifiedBundleTooltipColumnsPixels(this.contents.size());
    }

    @ModifyConstant(method = "drawProgressbar", constant = @Constant(intValue = 48))
    private int removeVanillaProgressBarTextOffset(int constant) {
        if (!BundlesBeyond.isModEnabled() || this.contents.isEmpty()) return constant;
        return 0;
    }

    @WrapOperation(method = "drawProgressbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"))
    private void fractionalOffsetProgressBarText(GuiGraphics instance, Font textRenderer, Component text, int centerX, int y, int color, Operation<Void> original) {
        if (!BundlesBeyond.isModEnabled() || this.contents.isEmpty()) {
            original.call(instance, textRenderer, text, centerX, y, color);
            return;
        }
        instance.pose()
                //? if >=1.21.8 {
                .pushMatrix();
                //?} else {
                /*.pushPose();
                *///?}
        float offset = BundleTooltipAdditions.getModifiedBundleTooltipColumnsPixels(this.contents.size()) / 2f;
        instance.pose().translate(offset, 0/*? if <1.21.8 {*//*, 0*//*?}*/);
        original.call(instance, textRenderer, text, centerX, y, color);
        instance.pose()
                //? if >=1.21.8 {
                .popMatrix();
                //?} else {
                /*.popPose();
                *///?}
    }

    @ModifyReturnValue(method = "getProgressBarFillText", at = @At("TAIL"))
    private Component modifyProgressBarLabel(Component original) {
        if (!BundlesBeyond.isModEnabled()) return original;
        return Component.literal(contents.weight().multiplyBy(Fraction.getFraction(64, 1)).getNumerator() + "/64");
    }

    @ModifyConstant(method = "itemGridHeight", constant = @Constant(intValue = 24))
    private int changeRowHeight(int constant) {
        if (!BundlesBeyond.isModEnabled()) return constant;
        return BundlesBeyondConfig.instance().slotSize;
    }
    //? if =1.21.3 {
    /*@ModifyConstant(method = "renderSlot", constant = @Constant(intValue = 24))
    private int modifySlotTextureSize(int constant) {
        if (!BundlesBeyond.isModEnabled()) return constant;
        return BundlesBeyondConfig.instance().slotSize;
    }
    *///?}

    //? if >=1.21.8 {
    @WrapOperation(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    //?}
    //? if =1.21.4 {
    /*@WrapOperation(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Ljava/util/function/Function;Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    *///?}
    //? if >=1.21.4 {
    private void fractionalSlotOffset(GuiGraphics instance,
                                      /*? if >=1.21.8 {*/RenderPipeline pipeline,
                                      /*?} else {*//*Function<ResourceLocation, RenderType> renderLayers,*//*?}*/
                                      ResourceLocation sprite, int x, int y, int width, int height, Operation<Void> original) {
        if (!BundlesBeyond.isModEnabled()) {
            original.call(instance, /*? if =1.21.4 {*//*renderLayers*//*?} else {*/pipeline/*?}*/, sprite, x, y, width, height);
            return;
        }
        instance.pose()
                //? if >=1.21.8 {
                .pushMatrix();
                //?} else {
                /*.pushPose();
                 *///?}
        float offset = (BundlesBeyondConfig.instance().slotSize - 24) / 2f;
        instance.pose().translate(offset, offset/*? if <1.21.8 {*//*, 0*//*?}*/);
        original.call(instance, /*? if =1.21.4 {*//*renderLayers*//*?} else {*/pipeline/*?}*/, sprite, x, y, width, height);
        instance.pose()
                //? if >=1.21.8 {
                .popMatrix();
                //?} else {
                /*.popPose();
                *///?}
    }
    //?}

    @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;III)V"))
    private void fractionalItemOffset(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics drawContext) {
        if (!BundlesBeyond.isModEnabled()) return;
        drawContext.pose()
                //? if >=1.21.8 {
                .pushMatrix();
                //?} else {
                /*.pushPose();
                *///?}
        float offset = (BundlesBeyondConfig.instance().slotSize - 16) / 2f;
        drawContext.pose().translate(offset, offset/*? if <1.21.8 {*//*, 0*//*?}*/);
    }

    @ModifyConstant(method = "renderSlot", constant = @Constant(intValue = 4))
    private int removeVanillaItemOffset(int constant) {
        if (!BundlesBeyond.isModEnabled()) return constant;
        return 0;
    }

    @Inject(method = "renderSlot", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V",
            shift = At.Shift.AFTER))
    private void resetItemOffset(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics drawContext) {
        if (!BundlesBeyond.isModEnabled()) return;
        drawContext.pose()
                //? if >=1.21.8 {
                .popMatrix();
                //?} else {
                /*.popPose();
                *///?}
    }
}

package o7410.bundlesbeyond.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
//? if >=1.21.8 {
import com.mojang.blaze3d.pipeline.RenderPipeline;
//?} else {
/*import net.minecraft.client.renderer.RenderType;
import java.util.function.Function;
*///?}
import net.minecraft.resources./*$ resource_location {*/Identifier/*$}*/;
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
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ClientBundleTooltip.class)
public abstract class ClientBundleTooltipMixin {
    @Unique private static final /*$ resource_location {*/Identifier/*$}*/ SLOT_BACKGROUND_SPRITE = /*$ resource_location {*/Identifier/*$}*/.withDefaultNamespace("container/slot");
    @Unique private static final /*$ resource_location {*/Identifier/*$}*/ SLOT_HIGHLIGHT_BACK_SPRITE = /*$ resource_location {*/Identifier/*$}*/.withDefaultNamespace("container/slot_highlight_back");
    @Unique private static final /*$ resource_location {*/Identifier/*$}*/ SLOT_HIGHLIGHT_FRONT_SPRITE = /*$ resource_location {*/Identifier/*$}*/.withDefaultNamespace("container/slot_highlight_front");
    @Shadow @Final private BundleContents contents;

    @ModifyConstant(method = "slotCount", constant = @Constant(intValue = 12))
    private int changeMaxVisibleSlots(int constant) {
        if (!BundlesBeyond.isModEnabled()) return constant;
        return Integer.MAX_VALUE;
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

    @ModifyArgs(
            method = "renderBundleWithItemsTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientBundleTooltip;renderSlot(IIILjava/util/List;ILnet/minecraft/client/gui/Font;Lnet/minecraft/client/gui/GuiGraphics;)V"
            )
    )
    private void reverseItems(Args args, Font font, int x, int y, int width, int height, GuiGraphics guiGraphics, @Local(ordinal = 7) int rowIndexFromOne, @Local(ordinal = 8) int columnIndexFromOne) {
        if (!BundlesBeyond.isModEnabled() || !BundlesBeyondConfig.instance().reverseView) return;
        args.set(1, x + (columnIndexFromOne - 1) * BundlesBeyondConfig.instance().slotSize);
        args.set(2, y + (rowIndexFromOne - 1) * BundlesBeyondConfig.instance().slotSize);
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
        instance.pose()./*$ push_matrix {*/pushMatrix();/*$}*/
        float offset = BundleTooltipAdditions.getModifiedBundleTooltipColumnsPixels(this.contents.size()) / 2f;
        instance.pose().translate(offset, 0/*? if <1.21.8 {*//*, 0*//*?}*/);
        original.call(instance, textRenderer, text, centerX, y, color);
        instance.pose()./*$ pop_matrix {*/popMatrix();/*$}*/
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

    @ModifyConstant(method = "renderSlot", constant = @Constant(intValue = 24))
    private int modifySlotTextureSize(int constant) {
        if (!BundlesBeyond.isModEnabled()) return constant;
        //? if =1.21.3 {
        /*return BundlesBeyondConfig.instance().slotSize;
        *///?} else {
        if (BundlesBeyondConfig.instance().containerSlots) return BundlesBeyondConfig.instance().slotSize;
        return constant;
        //?}
    }

    //? if >=1.21.11 {
    @WrapOperation(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    //?} else if >=1.21.8 {
    /*@WrapOperation(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    *///?} else if >=1.21.4 {
    /*@WrapOperation(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Ljava/util/function/Function;Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    *///?}
    //? if >=1.21.4 {
    private void fractionalSlotOffset(GuiGraphics instance, /*? if >=1.21.8 {*/RenderPipeline/*?} else {*//*Function<ResourceLocation, RenderType>*//*?}*/ render,
                                      /*$ resource_location {*/Identifier/*$}*/ sprite, int x, int y, int width, int height, Operation<Void> original) {
        if (!BundlesBeyond.isModEnabled() || BundlesBeyondConfig.instance().containerSlots) {
            original.call(instance, render, sprite, x, y, width, height);
            return;
        }
        instance.pose()./*$ push_matrix {*/pushMatrix();/*$}*/
        float offset = (BundlesBeyondConfig.instance().slotSize - 24) / 2f;
        instance.pose().translate(offset, offset/*? if <1.21.8 {*//*, 0*//*?}*/);
        original.call(instance, render, sprite, x, y, width, height);
        instance.pose()./*$ pop_matrix {*/popMatrix();/*$}*/
    }
    //?}

    @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;III)V"))
    private void fractionalItemOffset(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics guiGraphics) {
        if (!BundlesBeyond.isModEnabled()) return;
        guiGraphics.pose()./*$ push_matrix {*/pushMatrix();/*$}*/
        float offset = (BundlesBeyondConfig.instance().slotSize - 16) / 2f;
        guiGraphics.pose().translate(offset, offset/*? if <1.21.8 {*//*, 0*//*?}*/);
    }

    @ModifyConstant(method = "renderSlot", constant = @Constant(intValue = 4))
    private int removeVanillaItemOffset(int constant) {
        if (!BundlesBeyond.isModEnabled()) return constant;
        return 0;
    }

    @Inject(
            method = "renderSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V",
                    shift = At.Shift.AFTER
            )
    )
    private void resetItemOffset(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics guiGraphics) {
        if (!BundlesBeyond.isModEnabled()) return;
        guiGraphics.pose()./*$ pop_matrix {*/popMatrix();/*$}*/
    }

    @ModifyExpressionValue(
            method = "renderSlot",
            at = @At(
                    value = "FIELD",
                    //? if >=1.21.11 {
                    target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientBundleTooltip;SLOT_BACKGROUND_SPRITE:Lnet/minecraft/resources/Identifier;",
                    //?} else {
                    /*target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientBundleTooltip;SLOT_BACKGROUND_SPRITE:Lnet/minecraft/resources/ResourceLocation;",
                    *///?}
                    opcode = Opcodes.GETSTATIC
            )
    )
    private /*$ resource_location {*/Identifier/*$}*/ changeSlotBackground(/*$ resource_location {*/Identifier/*$}*/ original) {
        if (!BundlesBeyond.isModEnabled() || !BundlesBeyondConfig.instance().containerSlots) return original;
        return SLOT_BACKGROUND_SPRITE;
    }

    @WrapOperation(
            method = "renderSlot",
            at = @At(
                    value = "INVOKE",
                    //? if >=1.21.11 {
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
                    //?} else if >=1.21.8 {
                    /*target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIII)V",
                    *///?} else {
                    /*target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Ljava/util/function/Function;Lnet/minecraft/resources/ResourceLocation;IIII)V",
                    *///?}
                    ordinal = 0
            )
    )
    private void changeSlotHighlightBack(GuiGraphics instance, /*? if >=1.21.8 {*/RenderPipeline/*?} else {*//*Function<ResourceLocation, RenderType>*//*?}*/ render,
                                         /*$ resource_location {*/Identifier/*$}*/ sprite, int x, int y, int width, int height, Operation<Void> original) {
        if (!BundlesBeyond.isModEnabled() || !BundlesBeyondConfig.instance().containerSlots) {
            original.call(instance, render, sprite, x, y, width, height);
            return;
        }
        original.call(instance, render, SLOT_BACKGROUND_SPRITE, x, y, width, height);
        bundlesbeyond$renderHighlight(instance, render, SLOT_HIGHLIGHT_BACK_SPRITE, x, y, original);
    }

    @WrapOperation(
            method = "renderSlot",
            at = @At(
                    value = "INVOKE",
                    //? if >=1.21.11 {
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
                    //?} else if >=1.21.8 {
                    /*target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIII)V",
                    *///?} else {
                    /*target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Ljava/util/function/Function;Lnet/minecraft/resources/ResourceLocation;IIII)V",
                    *///?}
                    ordinal = 2
            )
    )
    private void changeSlotHighlightFront(GuiGraphics instance, /*? if >=1.21.8 {*/RenderPipeline/*?} else {*//*Function<ResourceLocation, RenderType>*//*?}*/ render,
                                          /*$ resource_location {*/Identifier/*$}*/ sprite, int x, int y, int width, int height, Operation<Void> original) {
        if (!BundlesBeyond.isModEnabled() || !BundlesBeyondConfig.instance().containerSlots) {
            original.call(instance, render, sprite, x, y, width, height);
            return;
        }
        bundlesbeyond$renderHighlight(instance, render, SLOT_HIGHLIGHT_FRONT_SPRITE, x, y, original);
    }

    @Unique
    private static void bundlesbeyond$renderHighlight(GuiGraphics instance, /*? if >=1.21.8 {*/RenderPipeline/*?} else {*//*Function<ResourceLocation, RenderType>*//*?}*/ render,
                                                      /*$ resource_location {*/Identifier/*$}*/ sprite, int x, int y, Operation<Void> original) {
        float offset = BundlesBeyondConfig.instance().slotSize / 6f;
        instance.pose()./*$ push_matrix {*/pushMatrix();/*$}*/
        instance.pose().translate(x - offset, y - offset/*? if <1.21.8 {*//*, 0*//*?}*/);
        float scaleFactor = BundlesBeyondConfig.instance().slotSize / 18f;
        instance.pose().scale(scaleFactor/*? if <1.21.8 {*//*, scaleFactor, scaleFactor*//*?}*/);
        original.call(instance, render, sprite, 0, 0, 24, 24);
        instance.pose()./*$ pop_matrix {*/popMatrix();/*$}*/
    }
}

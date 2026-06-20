package o7410.bundlesbeyond.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
//? if >=1.21.8 {
import com.mojang.blaze3d.pipeline.RenderPipeline;
//?} else {
/*import net.minecraft.client.renderer.RenderType;
import java.util.function.Function;
*///?}
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

@Mixin(ClientBundleTooltip.class)
public abstract class ClientBundleTooltipMixin {
    @Unique private static final Identifier SLOT_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("container/slot");
    @Unique private static final Identifier SLOT_HIGHLIGHT_BACK_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_back");
    @Unique private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_front");

    @Shadow @Final private BundleContents contents;

    //? if >=26.1 {
    @Unique private static int lastBundleSize = 0;

    @Inject(method = "extractImage", at = @At("HEAD"))
    private void captureBundleSize(CallbackInfo ci) {
        lastBundleSize = this.contents.size();
    }
    //?}

    @ModifyConstant(method = "slotCount", constant = @Constant(intValue = 12))
    private int changeMaxVisibleSlots(int constant) {
        if (!BundlesBeyond.isModEnabled()) return constant;
        return Integer.MAX_VALUE;
    }

    //~ if >=26.1 render -> extract
    @ModifyVariable(method = "extractBundleWithItemsTooltip", at = @At("STORE"))
    private boolean changeIfToDrawExtraItemsCount(boolean original) {
        if (!BundlesBeyond.isModEnabled()) return original;
        return false;
    }

    @ModifyConstant(
            //~ if >=26.1 render -> extract
            method = "extractBundleWithItemsTooltip",
            constant = @Constant(intValue = 4),
            slice = @Slice(
                    to = @At(
                            value = "INVOKE",
                            //~ if >=26.1 draw -> extract
                            target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientBundleTooltip;extractSelectedItemTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/client/gui/GuiGraphicsExtractor;III)V"
                    )
            )
    )
    private int modifyDrawnColumnsCount(int constant) {
        if (!BundlesBeyond.isModEnabled()) return constant;
        return BundleTooltipAdditions.getModifiedBundleTooltipColumns(this.contents.size());
    }

    //~ if >=26.1 render -> extract
    @ModifyConstant(method = "extractBundleWithItemsTooltip", constant = @Constant(intValue = 96))
    private int modifyRightAlignmentForItems(int constant) {
        if (!BundlesBeyond.isModEnabled()) return constant;
        if (BundlesBeyondConfig.instance().reverseView) {
            // x: replace the addition that gets the right from the left by a subtraction by slotSize to account for the index starting with 1
            return -BundlesBeyondConfig.instance().slotSize;
        } else {
            // add this width to the left to get the right
            return BundleTooltipAdditions.getModifiedBundleTooltipColumnsPixels(this.contents.size());
        }
    }

    //~ if >=26.1 render -> extract
    @ModifyConstant(method = "extractBundleWithItemsTooltip", constant = @Constant(intValue = 24))
    private int modifySlotSizeSlotPosition(int constant) {
        if (!BundlesBeyond.isModEnabled()) return constant;
        if (!BundlesBeyondConfig.instance().reverseView) {
            return BundlesBeyondConfig.instance().slotSize;
        } else {
            // change '... - slotSize * number' to an addition by negating the slotSize
            return -BundlesBeyondConfig.instance().slotSize;
        }
    }

    @ModifyExpressionValue(
            //~ if >=26.1 render -> extract
            method = "extractBundleWithItemsTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientBundleTooltip;gridSizeY()I",
                    ordinal = 0
            )
    )
    private int modifySlotSizeStartPosition(int original) {
        if (!BundlesBeyond.isModEnabled()) return original;
        if (BundlesBeyondConfig.instance().reverseView) {
            // y: replace the addition that gets the bottom from the top by a subtraction by slotSize to account for the index starting with 1
            return 1;
        } else {
            return original;
        }
    }

    @ModifyConstant(method = "getContentXOffset", constant = @Constant(intValue = 96))
    private /*? if >=26.1 {*/static/*?}*/ int modifyXMargin(int constant) {
        //$ size
        int size = lastBundleSize;
        if (!BundlesBeyond.isModEnabled() || size == 0) return constant;
        return BundleTooltipAdditions.getModifiedBundleTooltipColumnsPixels(size);
    }

    @ModifyConstant(method = "getWidth", constant = @Constant(intValue = 96))
    private int changeTooltipWidth(int constant) {
        if (!BundlesBeyond.isModEnabled() || this.contents.isEmpty()) return constant;
        return BundleTooltipAdditions.getModifiedBundleTooltipColumnsPixels(this.contents.size());
    }

    @ModifyConstant(method = "getProgressBarFill", constant = @Constant(intValue = 94))
    private /*? if >=26.1 {*/static/*?}*/ int changeProgressBarFill(int constant) {
        //$ size
        int size = lastBundleSize;
        if (!BundlesBeyond.isModEnabled() || size == 0) return constant;
        return BundleTooltipAdditions.getModifiedBundleTooltipColumnsPixels(size) - 2;
    }

    @ModifyConstant(method = "gridSizeY", constant = @Constant(intValue = 4))
    private int modifyItemsPerRow(int constant) {
        if (!BundlesBeyond.isModEnabled()) return constant;
        return BundleTooltipAdditions.getModifiedBundleTooltipColumns(this.contents.size());
    }

    //~ if >=26.1 draw -> extract
    @ModifyConstant(method = "extractProgressbar", constant = @Constant(intValue = 96))
    private /*? if >=26.1 {*/static/*?}*/ int changeProgressBorderWidth(int constant) {
        //$ size
        int size = lastBundleSize;
        if (!BundlesBeyond.isModEnabled() || size == 0) return constant;
        return BundleTooltipAdditions.getModifiedBundleTooltipColumnsPixels(size);
    }

    //~ if >=26.1 draw -> extract
    @ModifyConstant(method = "extractProgressbar", constant = @Constant(intValue = 48))
    private /*? if >=26.1 {*/static/*?}*/ int removeVanillaProgressBarTextOffset(int constant) {
        //$ size
        int size = lastBundleSize;
        if (!BundlesBeyond.isModEnabled() || size == 0) return constant;
        return 0;
    }

    //~ if >=26.1 draw -> extract
    //~ if >=26.1 drawCenteredString -> centeredText
    @WrapOperation(method = "extractProgressbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;centeredText(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"))
    private /*? if >=26.1 {*/static/*?}*/ void fractionalOffsetProgressBarText(GuiGraphicsExtractor instance, Font font, Component text, int x, int y, int color, Operation<Void> original) {
        //$ size
        int size = lastBundleSize;
        if (!BundlesBeyond.isModEnabled() || size == 0) {
            original.call(instance, font, text, x, y, color);
            return;
        }
        instance.pose().pushMatrix();
        float offset = BundleTooltipAdditions.getModifiedBundleTooltipColumnsPixels(size) / 2f;
        //$ translate instance offset '0'
        instance.pose().translate(offset, 0);
        original.call(instance, font, text, x, y, color);
        instance.pose().popMatrix();
    }

    @ModifyReturnValue(method = "getProgressBarFillText", at = @At("TAIL"))
    //? if >=26.1 {
    private static Component modifyProgressBarLabel(Component original, Fraction weight) {
    //?} else {
    /*private Component modifyProgressBarLabel(Component original) {
        Fraction weight = this.contents.weight();
    *///?}
        if (!BundlesBeyond.isModEnabled()) return original;
        return Component.literal(weight.multiplyBy(Fraction.getFraction(64, 1)).getNumerator() + "/64");
    }

    @ModifyConstant(method = "itemGridHeight", constant = @Constant(intValue = 24))
    private int changeRowHeight(int constant) {
        if (!BundlesBeyond.isModEnabled()) return constant;
        return BundlesBeyondConfig.instance().slotSize;
    }

    //~ if >=26.1 render -> extract
    @ModifyConstant(method = "extractSlot", constant = @Constant(intValue = 24))
    private int modifySlotTextureSize(int constant) {
        if (!BundlesBeyond.isModEnabled()) return constant;
        //? if =1.21.3 {
        /*return BundlesBeyondConfig.instance().slotSize;
        *///?} else {
        if (BundlesBeyondConfig.instance().containerSlots) return BundlesBeyondConfig.instance().slotSize;
        return constant;
        //?}
    }

    //? if >=1.21.4 {
    //~ if >=26.1 render -> extract
    @WrapOperation(method = "extractSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private void fractionalSlotOffset(GuiGraphicsExtractor instance, RenderPipeline render,
                                      Identifier location, int x, int y, int width, int height, Operation<Void> original) {
        if (!BundlesBeyond.isModEnabled() || BundlesBeyondConfig.instance().containerSlots) {
            original.call(instance, render, location, x, y, width, height);
            return;
        }
        instance.pose().pushMatrix();
        float offset = (BundlesBeyondConfig.instance().slotSize - 24) / 2f;
        //$ translate instance offset offset
        instance.pose().translate(offset, offset);
        original.call(instance, render, location, x, y, width, height);
        instance.pose().popMatrix();
    }
    //?}

    //~ if >=26.1 render -> extract
    //~ if >=26.1 'renderItem(' -> 'item('
    @Inject(method = "extractSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;item(Lnet/minecraft/world/item/ItemStack;III)V"))
    private void fractionalItemOffset(CallbackInfo ci, @Local(argsOnly = true) GuiGraphicsExtractor graphics) {
        if (!BundlesBeyond.isModEnabled()) return;
        graphics.pose().pushMatrix();
        float offset = (BundlesBeyondConfig.instance().slotSize - 16) / 2f;
        //$ translate graphics offset offset
        graphics.pose().translate(offset, offset);
    }

    //~ if >=26.1 render -> extract
    @ModifyConstant(method = "extractSlot", constant = @Constant(intValue = 4))
    private int removeVanillaItemOffset(int constant) {
        if (!BundlesBeyond.isModEnabled()) return constant;
        return 0;
    }

    @Inject(
            //~ if >=26.1 render -> extract
            method = "extractSlot",
            at = @At(
                    value = "INVOKE",
                    //~ if >=26.1 renderItemDecorations -> itemDecorations
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V",
                    shift = At.Shift.AFTER
            )
    )
    private void resetItemOffset(CallbackInfo ci, @Local(argsOnly = true) GuiGraphicsExtractor graphics) {
        if (!BundlesBeyond.isModEnabled()) return;
        graphics.pose().popMatrix();
    }

    @ModifyExpressionValue(
            //~ if >=26.1 render -> extract
            method = "extractSlot",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientBundleTooltip;SLOT_BACKGROUND_SPRITE:Lnet/minecraft/resources/Identifier;",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private Identifier changeSlotBackground(Identifier original) {
        if (!BundlesBeyond.isModEnabled() || !BundlesBeyondConfig.instance().containerSlots) return original;
        return SLOT_BACKGROUND_SPRITE;
    }

    @WrapOperation(
            //~ if >=26.1 render -> extract
            method = "extractSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
                    ordinal = 0
            )
    )
    private void changeSlotHighlightBack(GuiGraphicsExtractor instance, RenderPipeline render,
                                         Identifier location, int x, int y, int width, int height, Operation<Void> original) {
        if (!BundlesBeyond.isModEnabled() || !BundlesBeyondConfig.instance().containerSlots) {
            original.call(instance, render, location, x, y, width, height);
            return;
        }
        original.call(instance, render, SLOT_BACKGROUND_SPRITE, x, y, width, height);
        bundlesbeyond$renderHighlight(instance, render, SLOT_HIGHLIGHT_BACK_SPRITE, x, y, original);
    }

    @WrapOperation(
            //~ if >=26.1 render -> extract
            method = "extractSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
                    ordinal = 2
            )
    )
    private void changeSlotHighlightFront(GuiGraphicsExtractor instance, RenderPipeline render,
                                          Identifier location, int x, int y, int width, int height, Operation<Void> original) {
        if (!BundlesBeyond.isModEnabled() || !BundlesBeyondConfig.instance().containerSlots) {
            original.call(instance, render, location, x, y, width, height);
            return;
        }
        bundlesbeyond$renderHighlight(instance, render, SLOT_HIGHLIGHT_FRONT_SPRITE, x, y, original);
    }

    @Unique
    private static void bundlesbeyond$renderHighlight(GuiGraphicsExtractor instance, RenderPipeline render,
                                                      Identifier sprite, int x, int y, Operation<Void> original) {
        float offset = BundlesBeyondConfig.instance().slotSize / 6f;
        instance.pose().pushMatrix();
        //$ translate instance 'x - offset' 'y - offset'
        instance.pose().translate(x - offset, y - offset);
        float scaleFactor = BundlesBeyondConfig.instance().slotSize / 18f;
        //? if >=26.1 {
        instance.pose().scale(scaleFactor, scaleFactor);
        //?} else if >=1.21.8 {
        /*instance.pose().scale(scaleFactor);
        *///?} else {
        /*instance.pose().scale(scaleFactor, scaleFactor, 1);
        *///?}
        original.call(instance, render, sprite, 0, 0, 24, 24);
        instance.pose().popMatrix();
    }
}

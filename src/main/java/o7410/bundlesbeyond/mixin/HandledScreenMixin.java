package o7410.bundlesbeyond.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.BundleTooltipSubmenuHandler;
import net.minecraft.client.gui.tooltip.TooltipSubmenuHandler;
//? if >=1.21.10 {
import net.minecraft.client.input.KeyInput;
//?}
import net.minecraft.screen.slot.Slot;
import o7410.bundlesbeyond.BundleTooltipAdditions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Shadow @Nullable protected Slot focusedSlot;

    @Shadow @Final private List<TooltipSubmenuHandler> tooltipSubmenuHandlers;

    //? if <1.21.10 {
    /*@Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;handleHotbarKeyPressed(II)Z"), cancellable = true)
    private void bundleSubmenuKeyHandling(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
    *///?} else {
    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;handleHotbarKeyPressed(Lnet/minecraft/client/input/KeyInput;)Z"), cancellable = true)
    private void bundleSubmenuKeyHandling(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        int keyCode = input.key();
    //?}
        if (this.focusedSlot == null || !this.focusedSlot.hasStack()) {
            return;
        }
        for (TooltipSubmenuHandler tooltipSubmenuHandler : this.tooltipSubmenuHandlers) {
            if (
                    tooltipSubmenuHandler instanceof BundleTooltipSubmenuHandler &&
                    tooltipSubmenuHandler.isApplicableTo(this.focusedSlot) &&
                    BundleTooltipAdditions.handleKeybindsInBundleGui(this.focusedSlot, keyCode)
            ) {
                cir.setReturnValue(true);
            }
        }
    }

    @Unique
    protected List<TooltipSubmenuHandler> bundlesBeyond$getTooltipSubmenuHandlers() {
        return tooltipSubmenuHandlers;
    }
}

package o7410.bundlesbeyond.mixin;

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
import net.minecraft.client.gui.BundleMouseActions;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
//? if >=1.21.10 {
import net.minecraft.client.input.KeyEvent;
//?}
import net.minecraft.world.inventory.Slot;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin {
    @Shadow @Nullable protected Slot hoveredSlot;

    @Shadow @Final private List<ItemSlotMouseAction> itemSlotMouseActions;

    //? if <1.21.10 {
    /*@Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;checkHotbarKeyPressed(II)Z"), cancellable = true)
    private void bundleSubmenuKeyHandling(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
    *///?} else {
    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;checkHotbarKeyPressed(Lnet/minecraft/client/input/KeyEvent;)Z"), cancellable = true)
    private void bundleSubmenuKeyHandling(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
        int keyCode = input.key();
    //?}
        if (this.hoveredSlot == null || !this.hoveredSlot.hasItem()) {
            return;
        }
        for (ItemSlotMouseAction tooltipSubmenuHandler : this.itemSlotMouseActions) {
            if (
                    tooltipSubmenuHandler instanceof BundleMouseActions &&
                    tooltipSubmenuHandler.matches(this.hoveredSlot) &&
                    BundleTooltipAdditions.handleKeybindsInBundleGui(this.hoveredSlot, keyCode)
            ) {
                cir.setReturnValue(true);
            }
        }
    }

    @Unique
    protected List<ItemSlotMouseAction> bundlesBeyond$getTooltipSubmenuHandlers() {
        return itemSlotMouseActions;
    }
}

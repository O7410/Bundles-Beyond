package o7410.bundlesbeyond.mixin;

import net.minecraft.client.gui.BundleMouseActions;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
//? if >=1.21.10 {
import net.minecraft.client.input.KeyEvent;
//?}
import o7410.bundlesbeyond.BundleTooltipAdditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends HandledScreenMixin {
    @Shadow private boolean ignoreTextInput;

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;isCreativeSlot(Lnet/minecraft/world/inventory/Slot;)Z"), cancellable = true)
    //? if <1.21.10 {
    /*private void bundleSubmenuKeyHandling(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
    *///?} else {
    private void bundleSubmenuKeyHandling(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
        int keyCode = input.key();
    //?}
        if (this.hoveredSlot == null || !this.hoveredSlot.hasItem()) {
            return;
        }
        for (ItemSlotMouseAction tooltipSubmenuHandler : this.bundlesBeyond$getTooltipSubmenuHandlers()) {
            if (
                    tooltipSubmenuHandler instanceof BundleMouseActions &&
                    tooltipSubmenuHandler.matches(this.hoveredSlot) &&
                    BundleTooltipAdditions.handleKeybindsInBundleGui(this.hoveredSlot, keyCode)
            ) {
                this.ignoreTextInput = true;
                cir.setReturnValue(true);
            }
        }
    }

}

package o7410.bundlesbeyond.mixin;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.tooltip.BundleTooltipSubmenuHandler;
import net.minecraft.client.gui.tooltip.TooltipSubmenuHandler;
//? if >=1.21.10 {
/*import net.minecraft.client.input.KeyInput;
*///?}
import o7410.bundlesbeyond.BundleTooltipAdditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends HandledScreenMixin {
    @Shadow private boolean ignoreTypedCharacter;

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/CreativeInventoryScreen;isCreativeInventorySlot(Lnet/minecraft/screen/slot/Slot;)Z"), cancellable = true)
    //? if <1.21.10 {
    private void bundleSubmenuKeyHandling(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
    //?} else {
    /*private void bundleSubmenuKeyHandling(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        int keyCode = input.key();
    *///?}
        if (this.focusedSlot == null || !this.focusedSlot.hasStack()) {
            return;
        }
        for (TooltipSubmenuHandler tooltipSubmenuHandler : this.bundlesBeyond$getTooltipSubmenuHandlers()) {
            if (
                    tooltipSubmenuHandler instanceof BundleTooltipSubmenuHandler &&
                    tooltipSubmenuHandler.isApplicableTo(this.focusedSlot) &&
                    BundleTooltipAdditions.handleKeybindsInBundleGui(this.focusedSlot, keyCode)
            ) {
                this.ignoreTypedCharacter = true;
                cir.setReturnValue(true);
            }
        }
    }

}

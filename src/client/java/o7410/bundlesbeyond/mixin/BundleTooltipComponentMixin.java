package o7410.bundlesbeyond.mixin;

import net.minecraft.client.gui.tooltip.BundleTooltipComponent;
import net.minecraft.component.type.BundleContentsComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleTooltipComponent.class)
public class BundleTooltipComponentMixin {
    @Shadow @Final private BundleContentsComponent bundleContents;

    @Inject(method = "getNumVisibleSlots", at = @At("HEAD"), cancellable = true)
    private void changeNumberOfVisibleSlots(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(this.bundleContents.size());
    }

    @ModifyVariable(method = "drawNonEmptyTooltip", at = @At("STORE"))
    private boolean changeIfToDrawExtraItemsCount(boolean original) {
        return false;
    }
}

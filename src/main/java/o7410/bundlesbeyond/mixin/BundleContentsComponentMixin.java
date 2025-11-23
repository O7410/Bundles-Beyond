package o7410.bundlesbeyond.mixin;

import net.minecraft.world.item.component.BundleContents;
import o7410.bundlesbeyond.BundlesBeyond;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleContents.class)
public abstract class BundleContentsComponentMixin {
    @Inject(method = "getNumberOfItemsToShow", at = @At("HEAD"), cancellable = true)
    public void changeNumberOfStacksShown(CallbackInfoReturnable<Integer> cir) {
        if (!BundlesBeyond.isModEnabled()) return;
        cir.setReturnValue(((BundleContents) (Object) this).size());
    }
}

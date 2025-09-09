package o7410.bundlesbeyond.mixin;

import net.minecraft.component.type.BundleContentsComponent;
import o7410.bundlesbeyond.BundlesBeyond;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleContentsComponent.class)
public abstract class BundleContentsComponentMixin {
    @Inject(method = "getNumberOfStacksShown", at = @At("HEAD"), cancellable = true)
    public void changeNumberOfStacksShown(CallbackInfoReturnable<Integer> cir) {
        if (!BundlesBeyond.isModEnabled()) return;
        cir.setReturnValue(((BundleContentsComponent) (Object) this).size());
    }
}

package o7410.bundlesbeyond.mixin;

import net.minecraft.component.type.BundleContentsComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleContentsComponent.class)
public class BundleContentsComponentMixin {
    @Inject(method = "getNumberOfStacksShown", at = @At("HEAD"), cancellable = true)
    public void changeNumberOfStacksShown(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(((BundleContentsComponent) (Object) this).size());
    }
}

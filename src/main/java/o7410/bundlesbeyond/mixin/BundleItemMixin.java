package o7410.bundlesbeyond.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BundleItem;
import net.minecraft.screen.slot.Slot;
import o7410.bundlesbeyond.BundlesBeyond;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleItem.class)
public abstract class BundleItemMixin {
    @Inject(method = "onClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BundleItem;playInsertSound(Lnet/minecraft/entity/Entity;)V"))
    private void keepSelectedSlot(
            CallbackInfoReturnable<Boolean> cir,
            @Local(argsOnly = true) PlayerEntity player,
            @Local(argsOnly = true) Slot slot,
            @Local BundleContentsComponent.Builder builder,
            @Local BundleContentsComponent bundleContentsComponent
    ) {
        //? if <1.21.9 {
        if (!player.getWorld().isClient()) return;
        //?} else {
        /*if (!player.getEntityWorld().isClient()) return;
        *///?}
        if (!BundlesBeyond.isModEnabled()) return;
        if (!bundleContentsComponent.hasSelectedStack()) return;
        int selectedIndex = bundleContentsComponent.getSelectedStackIndex() + 1;
        builder.setSelectedStackIndex(selectedIndex);
        BundlesBeyond.sendBundleSelectedPacket(slot.id, selectedIndex);
    }
}

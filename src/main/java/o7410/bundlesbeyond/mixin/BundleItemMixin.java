package o7410.bundlesbeyond.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.component.BundleContents;
import o7410.bundlesbeyond.BundlesBeyond;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleItem.class)
public abstract class BundleItemMixin {
    @Inject(method = "overrideOtherStackedOnMe", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BundleItem;playInsertSound(Lnet/minecraft/world/entity/Entity;)V"))
    private void keepSelectedSlot(
            CallbackInfoReturnable<Boolean> cir,
            @Local(argsOnly = true) Player player,
            @Local(argsOnly = true) Slot slot,
            @Local BundleContents.Mutable builder,
            @Local BundleContents bundleContentsComponent
    ) {
        if (!player.level().isClientSide()) return;
        if (!bundleContentsComponent.hasSelectedItem()) return;
        int selectedIndex = bundleContentsComponent.getSelectedItem() + 1;
        builder.toggleSelectedItem(selectedIndex);
        BundlesBeyond.sendBundleSelectedPacket(slot.index, selectedIndex);
    }
}

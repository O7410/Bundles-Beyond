package o7410.bundlesbeyond.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BundleItem;
import net.minecraft.network.packet.c2s.play.BundleItemSelectedC2SPacket;
import net.minecraft.screen.slot.Slot;
import o7410.bundlesbeyond.BundlesBeyondClient;
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
        if (!player.getWorld().isClient || !BundlesBeyondClient.isModEnabled()) return;
        if (!bundleContentsComponent.hasSelectedStack()) return;
        int selectedIndex = bundleContentsComponent.getSelectedStackIndex() + 1;
        builder.setSelectedStackIndex(selectedIndex);
        ClientPlayNetworkHandler clientPlayNetworkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (clientPlayNetworkHandler != null) {
            clientPlayNetworkHandler.sendPacket(new BundleItemSelectedC2SPacket(slot.id, selectedIndex));
        }
    }
}

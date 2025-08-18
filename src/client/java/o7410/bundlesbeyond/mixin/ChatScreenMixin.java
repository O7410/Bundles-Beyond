package o7410.bundlesbeyond.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import o7410.bundlesbeyond.BundlesBeyondConfigScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @WrapOperation(method = "keyPressed",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V", ordinal = 0),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ChatScreen;sendMessage(Ljava/lang/String;Z)V"))
    )
    private void keepCustomScreenOpen(MinecraftClient instance, Screen screen, Operation<Void> original) {
        if (instance.currentScreen instanceof BundlesBeyondConfigScreen) return;
        original.call(instance, screen);
    }
}

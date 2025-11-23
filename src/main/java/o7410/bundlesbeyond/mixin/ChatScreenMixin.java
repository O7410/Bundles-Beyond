package o7410.bundlesbeyond.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import o7410.bundlesbeyond.BundlesBeyondConfigScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @WrapOperation(method = "keyPressed",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", ordinal = 0),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/ChatScreen;handleChatInput(Ljava/lang/String;Z)V"))
    )
    private void keepCustomScreenOpen(Minecraft instance, Screen screen, Operation<Void> original) {
        if (instance.screen instanceof BundlesBeyondConfigScreen) return;
        original.call(instance, screen);
    }
}

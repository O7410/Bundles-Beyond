package o7410.bundlesbeyond.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//~ if >=26.2 Minecraft -> 'gui.Gui'
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import o7410.bundlesbeyond.BundlesBeyondConfigScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @WrapOperation(method = "keyPressed",
            at = @At(
                    value = "INVOKE",
                    //~ if >=26.2 Minecraft -> 'gui/Gui'
                    target = "Lnet/minecraft/client/gui/Gui;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V",
                    ordinal = 0
            ),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/ChatScreen;handleChatInput(Ljava/lang/String;Z)V"))
    )
    //~ if >=26.2 Minecraft -> Gui
    private void keepCustomScreenOpen(Gui instance, Screen screen, Operation<Void> original) {
        //~ if >=26.2 screen -> 'screen()'
        if (instance.screen() instanceof BundlesBeyondConfigScreen) return;
        original.call(instance, screen);
    }
}

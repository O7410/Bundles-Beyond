package o7410.bundlesbeyond.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
//~ if >=26.1 'ClickType' -> 'ContainerInput'
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Invoker
    //~ if >=26.1 'ClickType' -> 'ContainerInput'
    void callSlotClicked(Slot slot, int slotId, int button, ContainerInput actionType);
}

package o7410.bundlesbeyond;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.BundleItemSelectedC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import o7410.bundlesbeyond.mixin.KeyBindingAccessor;
import org.lwjgl.glfw.GLFW;

public class BundleTooltipAdditions {

    public static boolean handleKeybindsInBundleGui(ItemStack stack, int slotId, int keyCode, int scanCode) {
        if (BundlesBeyondClient.modEnabledKeyModeOnToggle && keyCode == ((KeyBindingAccessor) BundlesBeyondClient.modEnabledKey).getBoundKey().getCode()) {
            BundlesBeyondClient.modEnabledWhenOnToggle = !BundlesBeyondClient.modEnabledWhenOnToggle;
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(Text.literal("Bundles Beyond " + (BundlesBeyondClient.modEnabledWhenOnToggle ? "enabled" : "disabled")), true);
            }
            int selectedIndex = BundleItem.getSelectedStackIndex(stack);
            if (BundlesBeyondClient.modEnabledWhenOnToggle) return true;
            int shownStacksWhenDisabled = BundleItem.getNumberOfStacksShown(stack);
            if (selectedIndex >= shownStacksWhenDisabled) {
                selectedIndex = -1;
                BundleItem.setSelectedStackIndex(stack, selectedIndex);
                ClientPlayNetworkHandler clientPlayNetworkHandler = MinecraftClient.getInstance().getNetworkHandler();
                if (clientPlayNetworkHandler != null) {
                    clientPlayNetworkHandler.sendPacket(new BundleItemSelectedC2SPacket(slotId, selectedIndex));
                }
            }
            return true;
        }

        if (!BundlesBeyondClient.isModEnabled()) return false;

        if (
                BundlesBeyondClient.scrollAxisKeybindMode == ScrollAxisKeybindMode.TOGGLE &&
                keyCode == ((KeyBindingAccessor) BundlesBeyondClient.scrollAxisKey).getBoundKey().getCode()
        ) {
            BundlesBeyondClient.scrollingToggledHorizontal = !BundlesBeyondClient.scrollingToggledHorizontal;
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(Text.literal("Now scrolling " + (BundlesBeyondClient.scrollingToggledHorizontal ? "horizontally" : "vertically")), true);
            }
            return true;
        }

        int selectedIndex = BundleItem.getSelectedStackIndex(stack);
        int size = BundleItem.getNumberOfStacksShown(stack);
        if (size == 0) return false;
        int width = BundleTooltipAdditions.getModifiedBundleTooltipColumns(size);
        int height = BundleTooltipAdditions.getModifiedBundleTooltipRows(size, width);
        switch (keyCode) {
            case GLFW.GLFW_KEY_W, GLFW.GLFW_KEY_UP -> selectedIndex = BundleTooltipAdditions.offsetVertical(size, width, height, selectedIndex, -1);
            case GLFW.GLFW_KEY_A, GLFW.GLFW_KEY_LEFT -> selectedIndex = BundleTooltipAdditions.offsetHorizontal(size, width, height, selectedIndex, -1);
            case GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_DOWN -> selectedIndex = BundleTooltipAdditions.offsetVertical(size, width, height, selectedIndex, 1);
            case GLFW.GLFW_KEY_D, GLFW.GLFW_KEY_RIGHT -> selectedIndex = BundleTooltipAdditions.offsetHorizontal(size, width, height, selectedIndex, 1);
            default -> {
                return false;
            }
        }
        if (selectedIndex == -1) return false;

        BundleItem.setSelectedStackIndex(stack, selectedIndex);
        ClientPlayNetworkHandler clientPlayNetworkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (clientPlayNetworkHandler != null) {
            clientPlayNetworkHandler.sendPacket(new BundleItemSelectedC2SPacket(slotId, selectedIndex));
        }
        return true;
    }

    public static int offsetVertical(int size, int width, int height, int selectedIndex, int offset) {

        if (height == 1) {
            return selectedIndex == -1 ? 0 : -1;
        }

        if (selectedIndex == -1) {
            selectedIndex = 0;
        }

        int emptySlotsAtTheStart = width * height - size;
        int gridIndex = selectedIndex + emptySlotsAtTheStart;
        int gridX = gridIndex % width;
        int gridY = gridIndex / width;

        gridY += offset;

        if (gridY < 0) {
            gridY += height;
        }

        if (gridY >= height) {
            gridY -= height;
        }

        if (gridY == 0 && gridX < emptySlotsAtTheStart) {
            gridY += offset;
            if (gridY < 0) {
                gridY += height;
            }
        }
        gridIndex = gridY * width + gridX;
        selectedIndex = gridIndex - emptySlotsAtTheStart;

        return selectedIndex;
    }

    public static int offsetHorizontal(int size, int width, int height, int selectedIndex, int offset) {

        if (size == 1) {
            return selectedIndex == 0 ? -1 : 0;
        }

        if (selectedIndex == -1) {
            selectedIndex = 0;
        }

        int emptySlotsAtTheStart = width * height - size;
        int gridIndex = selectedIndex + emptySlotsAtTheStart;
        int gridX = gridIndex % width;

        boolean isFirstRow = selectedIndex < (width - emptySlotsAtTheStart);

        int thisRowWidth = isFirstRow ? (width - emptySlotsAtTheStart) : width;
        int indexInRow = isFirstRow ? selectedIndex : gridX;

        indexInRow += offset;

        if (indexInRow < 0) {
            indexInRow += thisRowWidth;
        }

        if (indexInRow >= thisRowWidth) {
            indexInRow -= thisRowWidth;
        }

        gridX = isFirstRow ? indexInRow + emptySlotsAtTheStart : indexInRow;

        gridIndex = gridIndex / width * width + gridX;
        selectedIndex = gridIndex - emptySlotsAtTheStart;

        return selectedIndex;
    }

    public static int getModifiedBundleTooltipColumns(int size) {
        return Math.max(4, MathHelper.ceil(Math.sqrt(size)));
    }

    public static int getModifiedBundleTooltipRows(int size, int columns) {
        return MathHelper.ceilDiv(size, columns);
    }
}

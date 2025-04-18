package o7410.bundlesbeyond;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.BundleItemSelectedC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import o7410.bundlesbeyond.mixin.KeyBindingAccessor;
import org.lwjgl.glfw.GLFW;

public class BundleTooltipAdditions {

    public static boolean handleKeybindsInBundleGui(ItemStack stack, int slotId, int keyCode, int scanCode) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        BundlesBeyondConfig config = BundlesBeyondConfig.instance();
        if (config.modEnabledKeyModeOnToggle && keyCode == ((KeyBindingAccessor) BundlesBeyondClient.modEnabledKey).getBoundKey().getCode()) {
            config.modEnabledWhenOnToggle = !config.modEnabledWhenOnToggle;
            BundlesBeyondConfig.HANDLER.save();
            if (player != null) {
                player.sendMessage(Text.literal("Bundles Beyond " + (config.modEnabledWhenOnToggle ? "enabled" : "disabled")), true);
            }
            int selectedIndex = BundleItem.getSelectedStackIndex(stack);
            if (config.modEnabledWhenOnToggle) return true;
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

        if (config.scrollAxisKeybindMode == ScrollAxisKeybindMode.TOGGLE &&
                keyCode == ((KeyBindingAccessor) BundlesBeyondClient.scrollAxisKey).getBoundKey().getCode()) {
            config.scrollingToggledHorizontal = !config.scrollingToggledHorizontal;
            BundlesBeyondConfig.HANDLER.save();
            if (player != null) {
                player.sendMessage(Text.literal("Now scrolling " + (config.scrollingToggledHorizontal ? "horizontally" : "vertically")), true);
            }
            return true;
        }

        int selectedIndex = BundleItem.getSelectedStackIndex(stack);
        int size = BundleItem.getNumberOfStacksShown(stack);
        if (size == 0) return false;
        int width = BundleTooltipAdditions.getModifiedBundleTooltipColumns(size);
        int height = BundleTooltipAdditions.getModifiedBundleTooltipRows(size, width);
        GameOptions gameOptions = MinecraftClient.getInstance().options;
        int forwardCode = ((KeyBindingAccessor) gameOptions.forwardKey).getBoundKey().getCode();
        int leftCode = ((KeyBindingAccessor) gameOptions.leftKey).getBoundKey().getCode();
        int backCode = ((KeyBindingAccessor) gameOptions.backKey).getBoundKey().getCode();
        int rightCode = ((KeyBindingAccessor) gameOptions.rightKey).getBoundKey().getCode();
        if (keyCode == forwardCode || keyCode == GLFW.GLFW_KEY_UP) {
            selectedIndex = BundleTooltipAdditions.offsetVertical(size, width, height, selectedIndex, -1);
        } else if (keyCode == leftCode || keyCode == GLFW.GLFW_KEY_LEFT) {
            selectedIndex = BundleTooltipAdditions.offsetHorizontal(size, width, height, selectedIndex, -1);
        } else if (keyCode == backCode || keyCode == GLFW.GLFW_KEY_DOWN) {
            selectedIndex = BundleTooltipAdditions.offsetVertical(size, width, height, selectedIndex, 1);
        } else if (keyCode == rightCode || keyCode == GLFW.GLFW_KEY_RIGHT) {
            selectedIndex = BundleTooltipAdditions.offsetHorizontal(size, width, height, selectedIndex, 1);
        } else {
            return false;
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

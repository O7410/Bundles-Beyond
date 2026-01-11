package o7410.bundlesbeyond;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import o7410.bundlesbeyond.mixin.AbstractContainerScreenAccessor;
import org.lwjgl.glfw.GLFW;

public class BundleTooltipAdditions {

    public static boolean handleKeybindsInBundleGui(Slot slot, int keyCode) {
        ItemStack stack = slot.getItem();
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) return false;
        BundlesBeyondConfig config = BundlesBeyondConfig.instance();
        if (config.modEnabledState != ModEnabledState.HOLD_KEY && keyCode == BundlesBeyond.getKeyCode(BundlesBeyond.MOD_ENABLED_KEY)) {
            config.modEnabledState = config.modEnabledState == ModEnabledState.ON ? ModEnabledState.OFF : ModEnabledState.ON;
            BundlesBeyondConfig.save();
            player.displayClientMessage(Component.literal("Bundles Beyond " + (config.modEnabledState == ModEnabledState.ON ? "enabled" : "disabled")), true);
            int selectedIndex = BundleItem.getSelectedItem(stack);
            if (config.modEnabledState == ModEnabledState.ON) return true;
            int shownStacksWhenDisabled = BundleItem.getNumberOfItemsToShow(stack);
            if (selectedIndex >= shownStacksWhenDisabled) {
                selectedIndex = -1;
                BundleItem.toggleSelectedItem(stack, selectedIndex);
                BundlesBeyond.sendBundleSelectedPacket(slot.index, selectedIndex);
            }
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            if (client.screen instanceof AbstractContainerScreen<?> handledScreen) {
                AbstractContainerMenu currentScreenHandler = player.containerMenu;
                int button = currentScreenHandler.getCarried().isEmpty() ? 1 : 0; // right : left
                ((AbstractContainerScreenAccessor) handledScreen).callSlotClicked(slot, slot.index, button, ClickType.PICKUP);
            }
            return true;
        }

        if (!BundlesBeyond.isModEnabled()) return false;

        switch (keyCode) {
            case GLFW.GLFW_KEY_EQUAL, GLFW.GLFW_KEY_KP_ADD -> {
                if (config.slotSize < 24) {
                    config.slotSize++;
                    String message = "Slot size is now: " + config.slotSize + (config.slotSize == 24 ? " (Vanilla)" : "");
                    player.displayClientMessage(Component.literal(message), true);
                    BundlesBeyondConfig.save();
                }
            }
            case GLFW.GLFW_KEY_MINUS, GLFW.GLFW_KEY_KP_SUBTRACT -> {
                if (config.slotSize > 18) {
                    config.slotSize--;
                    String message = "Slot size is now: " + config.slotSize + (config.slotSize == 24 ? " (Vanilla)" : "");
                    player.displayClientMessage(Component.literal(message), true);
                    BundlesBeyondConfig.save();
                }
            }
        }

        if ((config.scrollMode == ScrollMode.HORIZONTAL || config.scrollMode == ScrollMode.VERTICAL) &&
                keyCode == BundlesBeyond.getKeyCode(BundlesBeyond.SCROLL_AXIS_KEY)) {
            config.scrollMode = config.scrollMode == ScrollMode.HORIZONTAL ? ScrollMode.VERTICAL : ScrollMode.HORIZONTAL;
            BundlesBeyondConfig.save();
            player.displayClientMessage(Component.literal("Now scrolling " + (config.scrollMode == ScrollMode.HORIZONTAL ? "horizontally" : "vertically")), true);
            return true;
        }

        int selectedIndex = BundleItem.getSelectedItem(stack);
        int size = BundleItem.getNumberOfItemsToShow(stack);
        if (size == 0) return false;
        int width = BundleTooltipAdditions.getModifiedBundleTooltipColumns(size);
        int height = BundleTooltipAdditions.getModifiedBundleTooltipRows(size, width);
        Options gameOptions = client.options;
        int forwardCode = BundlesBeyond.getKeyCode(gameOptions.keyUp);
        int leftCode = BundlesBeyond.getKeyCode(gameOptions.keyLeft);
        int backCode = BundlesBeyond.getKeyCode(gameOptions.keyDown);
        int rightCode = BundlesBeyond.getKeyCode(gameOptions.keyRight);
        boolean isUp = keyCode == forwardCode || keyCode == GLFW.GLFW_KEY_UP;
        boolean isDown = keyCode == backCode || keyCode == GLFW.GLFW_KEY_DOWN;
        boolean isLeft = keyCode == leftCode || keyCode == GLFW.GLFW_KEY_LEFT;
        boolean isRight = keyCode == rightCode || keyCode == GLFW.GLFW_KEY_RIGHT;
        if (isUp || isDown) {
            selectedIndex = BundleTooltipAdditions.offsetVertical(size, width, height, selectedIndex, isDown ? 1 : -1);
        } else if (isLeft || isRight) {
            selectedIndex = BundleTooltipAdditions.offsetHorizontal(size, width, height, selectedIndex, isRight ? 1 : -1);
        } else {
            return false;
        }
        if (selectedIndex == -1) return false;

        BundleItem.toggleSelectedItem(stack, selectedIndex);
        BundlesBeyond.sendBundleSelectedPacket(slot.index, selectedIndex);
        return true;
    }

    public static int offsetVertical(int size, int width, int height, int selectedIndex, int offset) {
        if (BundlesBeyondConfig.instance().reverseView) offset = -offset;
        int defaultSelectedIndex = BundlesBeyondConfig.instance().reverseView ? size - 1 : 0;

        if (height == 1) {
            return selectedIndex == -1 ? defaultSelectedIndex : -1;
        }

        if (selectedIndex == -1) {
            selectedIndex = defaultSelectedIndex;
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
            if (height == 2) return -1;
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
            return selectedIndex == -1 ? 0 : -1;
        }

        if (BundlesBeyondConfig.instance().reverseView) {
            if (selectedIndex == -1) {
                selectedIndex = size - 1;
                if (offset > 0) return selectedIndex;
            }
            offset = -offset;
        }

        int emptySlotsAtTheStart = width * height - size;
        int gridIndex = selectedIndex + emptySlotsAtTheStart;
        int gridX = gridIndex % width;

        boolean isFirstRow = selectedIndex < (width - emptySlotsAtTheStart);

        int thisRowWidth = isFirstRow ? (width - emptySlotsAtTheStart) : width;
        if (thisRowWidth == 1) {
            return selectedIndex == 0 ? -1 : 0;
        }
        int indexInRow = isFirstRow ? selectedIndex : gridX;

        indexInRow += offset;

        if (indexInRow < 0) {
            indexInRow = thisRowWidth - 1;
        }

        if (indexInRow >= thisRowWidth) {
            indexInRow = 0;
        }

        gridX = isFirstRow ? indexInRow + emptySlotsAtTheStart : indexInRow;

        gridIndex = gridIndex / width * width + gridX;
        selectedIndex = gridIndex - emptySlotsAtTheStart;

        return selectedIndex;
    }

    public static int getModifiedBundleTooltipColumns(int size) {
        return Math.max(4, Mth.ceil(Math.sqrt(size)));
    }

    public static int getModifiedBundleTooltipColumnsPixels(int size) {
        return getModifiedBundleTooltipColumns(size) * BundlesBeyondConfig.instance().slotSize;
    }

    public static int getModifiedBundleTooltipRows(int size, int columns) {
        return Mth.positiveCeilDiv(size, columns);
    }
}

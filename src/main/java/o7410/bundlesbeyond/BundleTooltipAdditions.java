package o7410.bundlesbeyond;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import o7410.bundlesbeyond.mixin.HandledScreenAccessor;
import org.lwjgl.glfw.GLFW;

public class BundleTooltipAdditions {

    public static boolean handleKeybindsInBundleGui(Slot slot, int keyCode) {
        ItemStack stack = slot.getStack();
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return false;
        BundlesBeyondConfig config = BundlesBeyondConfig.instance();
        if (config.modEnabledState != ModEnabledState.HOLD_KEY && keyCode == BundlesBeyond.getKeyCode(BundlesBeyond.MOD_ENABLED_KEY)) {
            config.modEnabledState = config.modEnabledState == ModEnabledState.ON ? ModEnabledState.OFF : ModEnabledState.ON;
            BundlesBeyondConfig.save();
            player.sendMessage(Text.literal("Bundles Beyond " + (config.modEnabledState == ModEnabledState.ON ? "enabled" : "disabled")), true);
            int selectedIndex = BundleItem.getSelectedStackIndex(stack);
            if (config.modEnabledState == ModEnabledState.ON) return true;
            int shownStacksWhenDisabled = BundleItem.getNumberOfStacksShown(stack);
            if (selectedIndex >= shownStacksWhenDisabled) {
                selectedIndex = -1;
                BundleItem.setSelectedStackIndex(stack, selectedIndex);
                BundlesBeyond.sendBundleSelectedPacket(slot.id, selectedIndex);
            }
            return true;
        }

        if (!BundlesBeyond.isModEnabled()) return false;

        if ((config.scrollMode == ScrollMode.HORIZONTAL || config.scrollMode == ScrollMode.VERTICAL) &&
                keyCode == BundlesBeyond.getKeyCode(BundlesBeyond.SCROLL_AXIS_KEY)) {
            config.scrollMode = config.scrollMode == ScrollMode.HORIZONTAL ? ScrollMode.VERTICAL : ScrollMode.HORIZONTAL;
            BundlesBeyondConfig.save();
            player.sendMessage(Text.literal("Now scrolling " + (config.scrollMode == ScrollMode.HORIZONTAL ? "horizontally" : "vertically")), true);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            if (client.currentScreen instanceof HandledScreen<?> handledScreen) {
                ScreenHandler currentScreenHandler = player.currentScreenHandler;
                int button = currentScreenHandler.getCursorStack().isEmpty() ? 1 : 0; // right : left
                ((HandledScreenAccessor) handledScreen).callOnMouseClick(slot, slot.id, button, SlotActionType.PICKUP);
            }
            return true;
        }

        switch (keyCode) {
            case GLFW.GLFW_KEY_EQUAL, GLFW.GLFW_KEY_KP_ADD -> {
                if (config.slotSize < 24) {
                    config.slotSize++;
                    String message = "Slot size is now: " + config.slotSize + (config.slotSize == 24 ? " (Vanilla)" : "");
                    player.sendMessage(Text.literal(message), true);
                    BundlesBeyondConfig.save();
                }
            }
            case GLFW.GLFW_KEY_MINUS, GLFW.GLFW_KEY_KP_SUBTRACT -> {
                if (config.slotSize > 18) {
                    config.slotSize--;
                    String message = "Slot size is now: " + config.slotSize + (config.slotSize == 24 ? " (Vanilla)" : "");
                    player.sendMessage(Text.literal(message), true);
                    BundlesBeyondConfig.save();
                }
            }
        }

        int selectedIndex = BundleItem.getSelectedStackIndex(stack);
        int size = BundleItem.getNumberOfStacksShown(stack);
        if (size == 0) return false;
        int width = BundleTooltipAdditions.getModifiedBundleTooltipColumns(size);
        int height = BundleTooltipAdditions.getModifiedBundleTooltipRows(size, width);
        GameOptions gameOptions = client.options;
        int forwardCode = BundlesBeyond.getKeyCode(gameOptions.forwardKey);
        int leftCode = BundlesBeyond.getKeyCode(gameOptions.leftKey);
        int backCode = BundlesBeyond.getKeyCode(gameOptions.backKey);
        int rightCode = BundlesBeyond.getKeyCode(gameOptions.rightKey);
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
        BundlesBeyond.sendBundleSelectedPacket(slot.id, selectedIndex);
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
            return selectedIndex == 0 ? -1 : 0;
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
        return Math.max(4, MathHelper.ceil(Math.sqrt(size)));
    }

    public static int getModifiedBundleTooltipColumnsPixels(int size) {
        return getModifiedBundleTooltipColumns(size) * BundlesBeyondConfig.instance().slotSize;
    }

    public static int getModifiedBundleTooltipRows(int size, int columns) {
        return MathHelper.ceilDiv(size, columns);
    }
}

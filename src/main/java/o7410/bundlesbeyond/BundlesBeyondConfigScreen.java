package o7410.bundlesbeyond;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
//? if <1.21.10 {
/*import net.minecraft.client.gui.navigation.CommonInputs;
 *///?} else {
import net.minecraft.client.input.KeyEvent;
//?}
//? if >=1.21.8 {
import net.minecraft.client.renderer.RenderPipelines;
//?} else {
/*import net.minecraft.client.renderer.RenderType;
 *///?}
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import o7410.bundlesbeyond.mixin.SliderWidgetAccessor;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class BundlesBeyondConfigScreen extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(BundlesBeyond.MOD_ID, "textures/gui/config.png");
    private static final int BACKGROUND_WIDTH = 222;
    private static final int BACKGROUND_HEIGHT = 148;
    private static final Component FAILED_TO_RELOAD_TEXT = Component.literal("Failed to reload").withStyle(ChatFormatting.RED);
    private static final Component FAILED_TO_SAVE_TEXT = Component.literal("Failed to save").withStyle(ChatFormatting.RED);

    @Nullable private final Screen parentScreen;
    private ModEnabledStateButton[] modEnabledStateButtons;
    private ScrollModeButton[] scrollModeButtons;
    private StringWidget failedToReload;
    private StringWidget failedToSave;

    public BundlesBeyondConfigScreen(@Nullable Screen parentScreen) {
        super(Component.literal("Bundles Beyond config"));
        this.parentScreen = parentScreen;
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);

        context.blit(
                //? if >=1.21.8 {
                RenderPipelines.GUI_TEXTURED,
                //?} else {
                /*RenderType::guiTextured,
                *///?}
                TEXTURE,
                (this.width - BACKGROUND_WIDTH) / 2,
                (this.height - BACKGROUND_HEIGHT) / 2,
                0.0F, 0.0F,
                BACKGROUND_WIDTH, BACKGROUND_HEIGHT,
                256, 256
        );
        context.drawString(
                this.font,
                "Mod Enabled State",
                (this.width - BACKGROUND_WIDTH) / 2 + 9,
                (this.height + BACKGROUND_HEIGHT) / 2 - 40,
                0xFFFFFFFF
        );
        context.drawCenteredString(
                this.font,
                "Scroll Mode",
                this.width / 2,
                (this.height - BACKGROUND_HEIGHT) / 2 + 5,
                0xFFFFFFFF
        );
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(
                Button.builder(Component.literal("Reload"), this::reloadConfig)
                        .bounds((this.width + BACKGROUND_WIDTH) / 2 - 49, (this.height + BACKGROUND_HEIGHT) / 2 - 30, 40, 20)
                        .tooltip(Tooltip.create(Component.literal("Reload config")))
                        .build());

        addModEnabledStateButtons();
        addScrollModeButtons();
        addFailedTextWidgets();

        this.addRenderableWidget(
                new SlotSizeSlider((this.width - BACKGROUND_WIDTH) / 2 + 9, (this.height - BACKGROUND_HEIGHT) / 2 + 85,
                        204, 20, BundlesBeyondConfig.instance().slotSize));

        updateButtons();
    }

    private void addFailedTextWidgets() {
        this.failedToReload = new StringWidget(
                (this.width + BACKGROUND_WIDTH) / 2 - this.font.width(FAILED_TO_RELOAD_TEXT) - 10,
                (this.height + BACKGROUND_HEIGHT) / 2 - 40,
                this.font.width(FAILED_TO_RELOAD_TEXT),
                9,
                FAILED_TO_RELOAD_TEXT,
                this.font
        );
        this.failedToReload.setTooltip(Tooltip.create(Component.literal("See log for details")));
        this.failedToReload.visible = false;
        this.addRenderableWidget(this.failedToReload);

        this.failedToSave = new StringWidget(
                (this.width + BACKGROUND_WIDTH) / 2 - this.font.width(FAILED_TO_SAVE_TEXT) - 10,
                (this.height + BACKGROUND_HEIGHT) / 2 - 40,
                this.font.width(FAILED_TO_SAVE_TEXT),
                9,
                FAILED_TO_SAVE_TEXT,
                this.font
        );
        this.failedToSave.setTooltip(Tooltip.create(Component.literal("See log for details")));
        this.failedToSave.visible = false;
        this.addRenderableWidget(this.failedToSave);
    }

    private void addScrollModeButtons() {
        int topY = (this.height - BACKGROUND_HEIGHT) / 2;
        int centerX = this.width / 2;
        ScrollModeButton scrollModeVanillaButton = this.addRenderableWidget(
                new ScrollModeButton(ScrollMode.VANILLA,
                        centerX - 100 - 2, topY + 15, 204, 20));

        ScrollModeButton scrollModeHorizontalButton = this.addRenderableWidget(
                new ScrollModeButton(ScrollMode.HORIZONTAL,
                        centerX - 100 - 2, topY + 38, 100, 20));

        ScrollModeButton scrollModeVerticalButton = this.addRenderableWidget(
                new ScrollModeButton(ScrollMode.VERTICAL,
                        centerX + 2, topY + 38, 100, 20));

        ScrollModeButton scrollModeHoldForHorizontalButton = this.addRenderableWidget(
                new ScrollModeButton(ScrollMode.HOLD_FOR_HORIZONTAL,
                        centerX + 2, topY + 61, 100, 20));

        ScrollModeButton scrollModeHoldForVerticalButton = this.addRenderableWidget(
                new ScrollModeButton(ScrollMode.HOLD_FOR_VERTICAL,
                        centerX - 100 - 2, topY + 61, 100, 20));

        this.scrollModeButtons = new ScrollModeButton[] {
                scrollModeVanillaButton,
                scrollModeHorizontalButton,
                scrollModeVerticalButton,
                scrollModeHoldForHorizontalButton,
                scrollModeHoldForVerticalButton
        };
    }

    private void addModEnabledStateButtons() {
        int bottomY = (this.height + BACKGROUND_HEIGHT) / 2;
        int leftX = (this.width - BACKGROUND_WIDTH) / 2;
        ModEnabledStateButton modEnabledStateOnButton = this.addRenderableWidget(
                new ModEnabledStateButton(ModEnabledState.ON,
                        leftX + 9, bottomY - 30, 34, 20));

        ModEnabledStateButton modEnabledStateOffButton = this.addRenderableWidget(
                new ModEnabledStateButton(ModEnabledState.OFF,
                        leftX + 46, bottomY - 30, 34, 20));

        ModEnabledStateButton modEnabledStateHoldKeyButton = this.addRenderableWidget(
                new ModEnabledStateButton(ModEnabledState.HOLD_KEY,
                        leftX + 83, bottomY - 30, 71, 20));

        this.modEnabledStateButtons = new ModEnabledStateButton[] {
                modEnabledStateOnButton,
                modEnabledStateOffButton,
                modEnabledStateHoldKeyButton
        };
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parentScreen);
    }

    private void reloadConfig(Button button) {
        this.failedToReload.visible = !BundlesBeyondConfig.load();
        if (this.failedToReload.visible) {
            this.failedToSave.visible = false;
        }
        this.updateButtons();
    }

    private void save() {
        failedToSave.visible = !BundlesBeyondConfig.save();
        if (failedToSave.visible) {
            failedToReload.visible = false;
        }
    }

    private void updateButtons() {
        BundlesBeyondConfig config = BundlesBeyondConfig.instance();

        for (ModEnabledStateButton modEnabledStateButton : modEnabledStateButtons) {
            modEnabledStateButton.active = config.modEnabledState != modEnabledStateButton.modEnabledState;
        }

        for (ScrollModeButton scrollModeButton : scrollModeButtons) {
            scrollModeButton.active = config.scrollMode != scrollModeButton.scrollMode;
        }
    }

    private class ScrollModeButton extends Button {
        public final ScrollMode scrollMode;

        public ScrollModeButton(ScrollMode scrollMode, int x, int y, int width, int height) {
            super(x, y, width, height, scrollMode.getShortNameText(), button -> {
                BundlesBeyondConfig.instance().scrollMode = scrollMode;
                save();
                updateButtons();
            }, DEFAULT_NARRATION);
            this.setTooltip(Tooltip.create(scrollMode.getDescriptionText()));
            this.scrollMode = scrollMode;
        }
    }

    private class ModEnabledStateButton extends Button {
        public final ModEnabledState modEnabledState;

        public ModEnabledStateButton(ModEnabledState modEnabledState, int x, int y, int width, int height) {
            super(x, y, width, height, modEnabledState.getShortNameText(), button -> {
                BundlesBeyondConfig.instance().modEnabledState = modEnabledState;
                save();
                updateButtons();
            }, DEFAULT_NARRATION);
            this.setTooltip(Tooltip.create(modEnabledState.getDescriptionText()));
            this.modEnabledState = modEnabledState;
        }
    }

    private class SlotSizeSlider extends AbstractSliderButton {
        private static final int MIN_VALUE = 18;
        private static final int MAX_VALUE = 24;

        public SlotSizeSlider(int x, int y, int width, int height, int slotSize) {
            super(x, y, width, height, null, calculateValue(slotSize));
            updateMessage();
        }

        private int calculateSlotSize() {
            return (int) Math.round((this.value * (MAX_VALUE - MIN_VALUE) + MIN_VALUE));
        }

        private static double calculateValue(int slotSize) {
            return (double) (slotSize - MIN_VALUE) / (MAX_VALUE - MIN_VALUE);
        }

        @Override
        protected void updateMessage() {
            int slotSize = calculateSlotSize();
            this.setMessage(Component.literal("Bundle Slot Size: " + slotSize + (slotSize == 24 ? " (Vanilla)" : "")));
        }

        @Override
        protected void applyValue() {
            int slotSize = calculateSlotSize();
            BundlesBeyondConfig config = BundlesBeyondConfig.instance();
            if (config.slotSize != slotSize) {
                config.slotSize = slotSize;
                save();
            }

            this.value = calculateValue(slotSize);
        }

        @Override
        //? if <1.21.10 {
        /*public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        *///?} else {
        public boolean keyPressed(KeyEvent input) {
            int keyCode = input.key();
        //?}
            SliderWidgetAccessor self = (SliderWidgetAccessor) this;
            //? if <1.21.10 {
            /*if (CommonInputs.selected(keyCode)) {
            *///?} else {
            if (input.isSelection()) {
            //?}
                self.setCanChangeValue(!self.getCanChangeValue());
                return true;
            }
            if (self.getCanChangeValue()) {
                boolean pressedLeft = keyCode == GLFW.GLFW_KEY_LEFT;
                if (pressedLeft || keyCode == GLFW.GLFW_KEY_RIGHT) {
                    int offset = pressedLeft ? -1 : 1;

                    this.value = calculateValue(Mth.clamp(calculateSlotSize() + offset, MIN_VALUE, MAX_VALUE));
                    this.applyValue();
                    this.updateMessage();

                    return true;
                }
            }

            return false;
        }
    }
}

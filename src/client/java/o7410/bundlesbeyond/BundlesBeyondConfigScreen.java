package o7410.bundlesbeyond;

//import net.minecraft.client.gl.RenderPipelines; // 1.21.8
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class BundlesBeyondConfigScreen extends Screen {
    private static final Identifier TEXTURE = Identifier.of(BundlesBeyondClient.MOD_ID, "textures/gui/config.png");
    private static final int BACKGROUND_WIDTH = 222;
    private static final int BACKGROUND_HEIGHT = 126;
    private static final Text FAILED_TO_RELOAD_TEXT = Text.literal("Failed to reload").formatted(Formatting.RED);
    private static final Text FAILED_TO_SAVE_TEXT = Text.literal("Failed to save").formatted(Formatting.RED);

    @Nullable private final Screen parentScreen;
    private ButtonWidget reloadConfigButton;
    private ModEnabledStateButton[] modEnabledStateButtons;
    private ScrollModeButton[] scrollModeButtons;
    private TextWidget failedToReload;
    private TextWidget failedToSave;

    public BundlesBeyondConfigScreen(@Nullable Screen parentScreen) {
        super(Text.literal("Bundles Beyond config"));
        this.parentScreen = parentScreen;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);

        context.drawTexture(
                RenderLayer::getGuiTextured, // 1.21.3
//                RenderPipelines.GUI_TEXTURED, // 1.21.8
                TEXTURE,
                (this.width - BACKGROUND_WIDTH) / 2,
                (this.height - BACKGROUND_HEIGHT) / 2,
                0.0F, 0.0F,
                BACKGROUND_WIDTH, BACKGROUND_HEIGHT,
                256, 256
        );
        context.drawTextWithShadow(
                this.textRenderer,
                "Mod Enabled State",
                (this.width - BACKGROUND_WIDTH) / 2 + 10,
                (this.height + BACKGROUND_HEIGHT) / 2 - 40,
                0xFFFFFF
        );
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                "Scroll Mode",
                this.width / 2,
                (this.height - BACKGROUND_HEIGHT) / 2 + 5,
                0xFFFFFF
        );
    }

    @Override
    protected void init() {
        super.init();
        int leftX = (this.width - BACKGROUND_WIDTH) / 2;
        int centerX = this.width / 2;
        int topY = (this.height - BACKGROUND_HEIGHT) / 2;
        int bottomY = (this.height + BACKGROUND_HEIGHT) / 2;
        this.reloadConfigButton = this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Reload"), this::reloadConfig)
                        .dimensions((this.width + BACKGROUND_WIDTH) / 2 - 50, (this.height + BACKGROUND_HEIGHT) / 2 - 30, 40, 20)
                        .tooltip(Tooltip.of(Text.literal("Reload config")))
                        .build());

        ModEnabledStateButton modEnabledStateOnButton = this.addDrawableChild(
                new ModEnabledStateButton(ModEnabledState.ON,
                        leftX + 10, bottomY - 30, 34, 20));

        ModEnabledStateButton modEnabledStateOffButton = this.addDrawableChild(
                new ModEnabledStateButton(ModEnabledState.OFF,
                        leftX + 47, bottomY - 30, 34, 20));

        ModEnabledStateButton modEnabledStateHoldKeyButton = this.addDrawableChild(
                new ModEnabledStateButton(ModEnabledState.HOLD_KEY,
                        leftX + 84, bottomY - 30, 71, 20));

        this.modEnabledStateButtons = new ModEnabledStateButton[] {
                modEnabledStateOnButton,
                modEnabledStateOffButton,
                modEnabledStateHoldKeyButton
        };

        ScrollModeButton scrollModeVanillaButton = this.addDrawableChild(
                new ScrollModeButton(ScrollMode.VANILLA,
                        centerX - 100 - 2, topY + 15, 203, 20));

        ScrollModeButton scrollModeHorizontalButton = this.addDrawableChild(
                new ScrollModeButton(ScrollMode.HORIZONTAL,
                        centerX - 100 - 2, topY + 38, 100, 20));

        ScrollModeButton scrollModeVerticalButton = this.addDrawableChild(
                new ScrollModeButton(ScrollMode.VERTICAL,
                        centerX + 2, topY + 38, 100, 20));

        ScrollModeButton scrollModeHoldForHorizontalButton = this.addDrawableChild(
                new ScrollModeButton(ScrollMode.HOLD_FOR_HORIZONTAL,
                        centerX + 2, topY + 61, 100, 20));

        ScrollModeButton scrollModeHoldForVerticalButton = this.addDrawableChild(
                new ScrollModeButton(ScrollMode.HOLD_FOR_VERTICAL,
                        centerX - 100 - 2, topY + 61, 100, 20));

        this.scrollModeButtons = new ScrollModeButton[] {
                scrollModeVanillaButton,
                scrollModeHorizontalButton,
                scrollModeVerticalButton,
                scrollModeHoldForHorizontalButton,
                scrollModeHoldForVerticalButton
        };

        this.failedToReload = new TextWidget(
                (this.width + BACKGROUND_WIDTH) / 2 - this.textRenderer.getWidth(FAILED_TO_RELOAD_TEXT) - 10,
                (this.height + BACKGROUND_HEIGHT) / 2 - 40,
                this.textRenderer.getWidth(FAILED_TO_RELOAD_TEXT),
                9,
                FAILED_TO_RELOAD_TEXT,
                this.textRenderer
        );
        this.failedToReload.setTooltip(Tooltip.of(Text.literal("See log for details")));
        this.failedToReload.visible = false;
        this.addDrawableChild(this.failedToReload);

        this.failedToSave = new TextWidget(
                (this.width + BACKGROUND_WIDTH) / 2 - this.textRenderer.getWidth(FAILED_TO_SAVE_TEXT) - 10,
                (this.height + BACKGROUND_HEIGHT) / 2 - 40,
                this.textRenderer.getWidth(FAILED_TO_SAVE_TEXT),
                9,
                FAILED_TO_SAVE_TEXT,
                this.textRenderer
        );
        this.failedToSave.setTooltip(Tooltip.of(Text.literal("See log for details")));
        this.failedToSave.visible = false;
        this.addDrawableChild(this.failedToSave);

        updateButtons();
    }

    @Override
    public void close() {
        this.client.setScreen(this.parentScreen);
    }

    private void reloadConfig(ButtonWidget button) {
        this.failedToReload.visible = !BundlesBeyondConfig.load();
        if (this.failedToReload.visible) {
            this.failedToSave.visible = false;
        }
        this.updateButtons();
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

    private class ScrollModeButton extends ButtonWidget {
        public final ScrollMode scrollMode;

        public ScrollModeButton(ScrollMode scrollMode, int x, int y, int width, int height) {
            super(x, y, width, height, scrollMode.getShortNameText(), button -> {
                BundlesBeyondConfig.instance().scrollMode = scrollMode;
                failedToSave.visible = !BundlesBeyondConfig.save();
                if (failedToSave.visible) {
                    failedToReload.visible = false;
                }
                updateButtons();
            }, DEFAULT_NARRATION_SUPPLIER);
            this.setTooltip(Tooltip.of(scrollMode.getDescriptionText()));
            this.scrollMode = scrollMode;
        }
    }

    private class ModEnabledStateButton extends ButtonWidget {
        public final ModEnabledState modEnabledState;

        public ModEnabledStateButton(ModEnabledState modEnabledState, int x, int y, int width, int height) {
            super(x, y, width, height, modEnabledState.getShortNameText(), button -> {
                BundlesBeyondConfig.instance().modEnabledState = modEnabledState;
                failedToSave.visible = !BundlesBeyondConfig.save();
                if (failedToSave.visible) {
                    failedToReload.visible = false;
                }
                updateButtons();
            }, DEFAULT_NARRATION_SUPPLIER);
            this.setTooltip(Tooltip.of(modEnabledState.getDescriptionText()));
            this.modEnabledState = modEnabledState;
        }
    }
}

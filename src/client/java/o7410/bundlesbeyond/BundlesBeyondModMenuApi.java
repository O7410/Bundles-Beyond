package o7410.bundlesbeyond;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class BundlesBeyondModMenuApi implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> YetAnotherConfigLib.create(BundlesBeyondConfig.HANDLER, (defaults, config, builder) ->
                        builder
                                .title(Text.literal("Bundles Beyond Config"))
                                .category(ConfigCategory.createBuilder()
                                        .name(Text.literal("Bundles Beyond Config"))
                                        .tooltip(Text.literal("The config for the Bundles Beyond mod"))
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Text.literal("Mod Enabled Keybind Mode"))
                                                .description(OptionDescription.of(Text.literal("The mode for the mod enabled keybind.\nCurrent keybind value: ")
                                                        .append(Text.keybind("key." + BundlesBeyondClient.MOD_ID + ".mod_enabled"))))
                                                .binding(defaults.modEnabledKeyModeOnToggle, () -> config.modEnabledKeyModeOnToggle, modEnabledKeyModeOnToggle -> config.modEnabledKeyModeOnToggle = modEnabledKeyModeOnToggle)
                                                .controller(option -> BooleanControllerBuilder.create(option).formatValue(value -> Text.literal(value ? "Toggle" : "Hold")))
                                                .build())
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Text.literal("Mod Enabled"))
                                                .description(OptionDescription.of(Text.literal("Enables or disables the mod.\nNote: only affects when the mod enabled keybind mode is on toggle")))
                                                .binding(defaults.modEnabledWhenOnToggle, () -> config.modEnabledWhenOnToggle, modEnabledWhenOnToggle -> config.modEnabledWhenOnToggle = modEnabledWhenOnToggle)
                                                .controller(TickBoxControllerBuilder::create)
                                                .build())
                                        .option(Option.<ScrollAxisKeybindMode>createBuilder()
                                                .name(Text.literal("Bundle Scroll Keybind Mode"))
                                                .description(bundleScrollKeybindMode -> {
                                                    List<Text> lines = new ArrayList<>();
                                                    lines.add(Text.literal("Mode of scroll axis keybind"));
                                                    lines.add(Text.literal("Current keybind value: ").append(Text.keybind("key." + BundlesBeyondClient.MOD_ID + ".scroll_axis")));
                                                    lines.add(Text.literal("Options:"));

                                                    ScrollAxisKeybindMode[] values = ScrollAxisKeybindMode.values();
                                                    List<MutableText> optionLines = new ArrayList<>();
                                                    for (ScrollAxisKeybindMode scrollKeybindMode : values) {
                                                        optionLines.add(Text.literal("> ").append(scrollKeybindMode.shortName));
                                                    }
                                                    optionLines.get(bundleScrollKeybindMode.ordinal()).formatted(Formatting.BOLD);
                                                    lines.addAll(optionLines);

                                                    lines.add(Text.literal("\n").append(bundleScrollKeybindMode.getDescriptionText()));

                                                    return OptionDescription.of(lines.toArray(Text[]::new));
                                                })
                                                .binding(defaults.scrollAxisKeybindMode,
                                                        () -> config.scrollAxisKeybindMode,
                                                        scrollAxisKeybindMode -> config.scrollAxisKeybindMode = scrollAxisKeybindMode)
                                                .controller(option -> EnumControllerBuilder.create(option)
                                                        .enumClass(ScrollAxisKeybindMode.class)
                                                        .formatValue(ScrollAxisKeybindMode::getShortNameText))
                                                .build())
                                        .build())
                                .save(BundlesBeyondConfig.HANDLER::save))
                .generateScreen(parentScreen);
    }
}

package o7410.bundlesbeyond;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.text.Text;
import o7410.bundlesbeyond.mixin.KeyBindingAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundlesBeyondClient implements ClientModInitializer {

    public static final String MOD_ID = "bundles-beyond";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static KeyBinding scrollAxisKey;
    public static KeyBinding modEnabledKey;

    public static boolean isModEnabled() {
        BundlesBeyondConfig config = BundlesBeyondConfig.instance();
        return config.modEnabledKeyModeOnToggle ? config.modEnabledWhenOnToggle : InputUtil.isKeyPressed(
                MinecraftClient.getInstance().getWindow().getHandle(),
                ((KeyBindingAccessor) BundlesBeyondClient.modEnabledKey).getBoundKey().getCode()
        );
    }

    @Override
    public void onInitializeClient() {
        scrollAxisKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + MOD_ID + ".scroll_axis",
                InputUtil.UNKNOWN_KEY.getCode(),
                "category." + MOD_ID + ".bundles_beyond"
        ));
        modEnabledKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + MOD_ID + ".mod_enabled",
                InputUtil.UNKNOWN_KEY.getCode(),
                "category." + MOD_ID + ".bundles_beyond"
        ));

        BundlesBeyondConfig.HANDLER.load();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal("bundlesbeyond")
                    .then(ClientCommandManager.literal("mod_enabled")
                            .executes(context -> {
                                context.getSource().sendFeedback(Text.literal("Bundles Beyond is currently " + (BundlesBeyondConfig.instance().modEnabledWhenOnToggle ? "enabled" : "disabled")));
                                return 0;
                            })
                            .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                    .executes(context -> executeSetModEnabled(context, BoolArgumentType.getBool(context, "value"))))
                    )
                    .then(ClientCommandManager.literal("mod_enabled_keybind_mode")
                            .executes(context -> {
                                context.getSource().sendFeedback(Text.literal("Mod enable keybind mode is currently " + (BundlesBeyondConfig.instance().modEnabledKeyModeOnToggle ? "toggle" : "hold")));
                                return 0;
                            })
                            .then(ClientCommandManager.literal("hold")
                                    .executes(context -> executeSetModEnabledKeybindMode(context, false)))
                            .then(ClientCommandManager.literal("toggle")
                                    .executes(context -> executeSetModEnabledKeybindMode(context, true)))
                    )
                    .then(ClientCommandManager.literal("scroll_axis_keybind_mode")
                            .executes(context -> {
                                context.getSource().sendFeedback(Text.literal("Scroll axis keybind mode is currently: ").append(BundlesBeyondConfig.instance().scrollAxisKeybindMode.getDescriptionText()));
                                return 0;
                            })
                            .then(ClientCommandManager.argument("mode", new EnumArgumentType<>(ScrollAxisKeybindMode.CODEC, ScrollAxisKeybindMode::values) {})
                                    .suggests((context, builder) -> {
                                        for (ScrollAxisKeybindMode scrollAxisKeybindMode : ScrollAxisKeybindMode.values()) {
                                            builder.suggest(scrollAxisKeybindMode.id, scrollAxisKeybindMode.getDescriptionText());
                                        }
                                        return builder.buildFuture();
                                    })
                                    .executes(context -> {
                                        BundlesBeyondConfig config = BundlesBeyondConfig.instance();
                                        config.scrollAxisKeybindMode = context.getArgument("mode", ScrollAxisKeybindMode.class);
                                        BundlesBeyondConfig.HANDLER.save();
                                        context.getSource().sendFeedback(Text.literal("Scroll axis keybind mode is now: ").append(config.scrollAxisKeybindMode.getDescriptionText()));
                                        return 0;
                                    })
                            )
                    )
                    .then(ClientCommandManager.literal("reloadconfig")
                            .executes(context -> {
                                if (BundlesBeyondConfig.HANDLER.load()) {
                                    context.getSource().sendFeedback(Text.literal("Reloaded Bundles Beyond config"));
                                } else {
                                    context.getSource().sendFeedback(Text.literal("Failed to reload Bundles Beyond config"));
                                }
                                return 0;
                            }))
            )
        );
    }

    private static int executeSetModEnabled(CommandContext<FabricClientCommandSource> context, boolean newValue) {
        BundlesBeyondConfig.instance().modEnabledWhenOnToggle = newValue;
        BundlesBeyondConfig.HANDLER.save();
        context.getSource().sendFeedback(Text.literal("Bundles Beyond is now " + (BundlesBeyondConfig.instance().modEnabledWhenOnToggle ? "enabled" : "disabled")));
        return 0;
    }

    private static int executeSetModEnabledKeybindMode(CommandContext<FabricClientCommandSource> context, boolean newModEnabledKeyOnToggle) {
        BundlesBeyondConfig.instance().modEnabledKeyModeOnToggle = newModEnabledKeyOnToggle;
        BundlesBeyondConfig.HANDLER.save();
        context.getSource().sendFeedback(Text.literal("Mod enable keybind mode is now " + (BundlesBeyondConfig.instance().modEnabledKeyModeOnToggle ? "toggle" : "hold")));
        return 0;
    }
}

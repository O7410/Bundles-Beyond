package o7410.bundlesbeyond;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.navigation.GuiNavigationType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

//? if fabric
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class BundlesBeyondCommand {
    public static <T extends CommandSource> void registerCommand(CommandDispatcher<T> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(LiteralArgumentBuilder.<T>literal("bundlesbeyond")
                .executes(BundlesBeyondCommand::configScreen)
                .then(LiteralArgumentBuilder.<T>literal("mod_enabled_state")
                        .executes(BundlesBeyondCommand::executeGetModEnabledState)
                        .then(RequiredArgumentBuilder.<T, ModEnabledState>argument("state", new EnumArgumentType<>(ModEnabledState.CODEC, ModEnabledState::values) {})
                                .suggests(BundlesBeyondCommand::getModEnabledStateSuggestions)
                                .executes(BundlesBeyondCommand::executeSetModEnabledState)))
                .then(LiteralArgumentBuilder.<T>literal("scroll_mode")
                        .executes(BundlesBeyondCommand::executeGetScrollMode)
                        .then(RequiredArgumentBuilder.<T, ScrollMode>argument("mode", new EnumArgumentType<>(ScrollMode.CODEC, ScrollMode::values) {})
                                .suggests(BundlesBeyondCommand::getScrollModeSuggestions)
                                .executes(BundlesBeyondCommand::executeSetScrollMode)
                        )
                )
                .then(LiteralArgumentBuilder.<T>literal("slot_size")
                        .executes(BundlesBeyondCommand::executeGetSlotSize)
                        .then(RequiredArgumentBuilder.<T, Integer>argument("size", IntegerArgumentType.integer(18))
                                .suggests(BundlesBeyondCommand::getSlotSizeSuggestions)
                                .executes(BundlesBeyondCommand::executeSetSlotSize)))
                .then(LiteralArgumentBuilder.<T>literal("reloadconfig")
                        .executes(BundlesBeyondCommand::executeReloadConfig))
        );
    }

    private static void sendFeedback(CommandContext<? extends CommandSource> context, Text text) {
        CommandSource source = context.getSource();

        //? if fabric {
        if (source instanceof FabricClientCommandSource fabricSource) {
            fabricSource.sendFeedback(text);
        }
        //?}
        if (source instanceof ServerCommandSource serverSource) {
            serverSource.sendFeedback(() -> text, false);
        }
    }

    private static void sendError(CommandContext<? extends CommandSource> context, Text text) {
        CommandSource source = context.getSource();
        //? if fabric {
        if (source instanceof FabricClientCommandSource fabricSource) {
            fabricSource.sendError(text);
        }
        //?}
        if (source instanceof ServerCommandSource serverSource) {
            serverSource.sendError(text);
        }
    }

    private static int configScreen(CommandContext<? extends CommandSource> context) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.setNavigationType(GuiNavigationType.NONE);
        client.setScreen(new BundlesBeyondConfigScreen(null));
        return 0;
    }

    private static int executeGetModEnabledState(CommandContext<? extends CommandSource> context) {
        ModEnabledState state = BundlesBeyondConfig.instance().modEnabledState;
        sendFeedback(context, Text.literal("Bundles Beyond enabled state is currently: ").append(state.getShortNameText()));
        return 0;
    }

    private static CompletableFuture<Suggestions> getModEnabledStateSuggestions(CommandContext<? extends CommandSource> context, SuggestionsBuilder builder) {
        for (ModEnabledState state : ModEnabledState.values()) {
            builder.suggest(state.id, state.getDescriptionText());
        }
        return builder.buildFuture();
    }

    private static int executeSetModEnabledState(CommandContext<? extends CommandSource> context) {
        ModEnabledState newModEnabledState = context.getArgument("state", ModEnabledState.class);
        BundlesBeyondConfig config = BundlesBeyondConfig.instance();
        sendFeedback(context, Text.literal("Bundles Beyond enabled state is now: ").append(newModEnabledState.getShortNameText()));
        if (config.modEnabledState != newModEnabledState) {
            config.modEnabledState = newModEnabledState;
            if (!BundlesBeyondConfig.save()) {
                sendError(context, Text.literal("Failed to save Bundles Beyond config"));
            }
        }
        return 0;
    }

    private static int executeGetScrollMode(CommandContext<? extends CommandSource> context) {
        sendFeedback(context, Text.literal("Scroll mode is currently: ").append(BundlesBeyondConfig.instance().scrollMode.getShortNameText()));
        return 0;
    }

    private static CompletableFuture<Suggestions> getScrollModeSuggestions(CommandContext<? extends CommandSource> context, SuggestionsBuilder builder) {
        for (ScrollMode scrollMode : ScrollMode.values()) {
            builder.suggest(scrollMode.id, scrollMode.getDescriptionText());
        }
        return builder.buildFuture();
    }

    private static int executeSetScrollMode(CommandContext<? extends CommandSource> context) {
        ScrollMode newMode = context.getArgument("mode", ScrollMode.class);
        sendFeedback(context, Text.literal("Scroll mode is now: ").append(newMode.getShortNameText()));
        BundlesBeyondConfig config = BundlesBeyondConfig.instance();
        if (config.scrollMode != newMode) {
            config.scrollMode = newMode;
            if (!BundlesBeyondConfig.save()) {
                sendError(context, Text.literal("Failed to save Bundles Beyond config"));
            }
        }
        return 0;
    }

    private static int executeGetSlotSize(CommandContext<? extends CommandSource> context) {
        int slotSize = BundlesBeyondConfig.instance().slotSize;
        sendFeedback(context, Text.literal("Slot size is currently: " + slotSize + (slotSize == 24 ? " (Vanilla)" : "")));
        return 0;
    }

    private static CompletableFuture<Suggestions> getSlotSizeSuggestions(CommandContext<? extends CommandSource> context, SuggestionsBuilder builder) {
        builder.suggest(24);
        return builder.buildFuture();
    }

    private static int executeSetSlotSize(CommandContext<? extends CommandSource> context) {
        int slotSize = IntegerArgumentType.getInteger(context, "size");
        sendFeedback(context, Text.literal("Slot size is now: " + slotSize + (slotSize == 24 ? " (Vanilla)" : "")));
        BundlesBeyondConfig config = BundlesBeyondConfig.instance();
        if (config.slotSize != slotSize) {
            config.slotSize = slotSize;
            if (!BundlesBeyondConfig.save()) {
                sendError(context, Text.literal("Failed to save Bundles Beyond config"));
            }
        }
        return 0;
    }

    private static int executeReloadConfig(CommandContext<? extends CommandSource> context) {
        if (BundlesBeyondConfig.load()) {
            sendFeedback(context, Text.literal("Reloaded Bundles Beyond config"));
        } else {
            sendError(context, Text.literal("Failed to reload Bundles Beyond config"));
        }
        return 0;
    }
}

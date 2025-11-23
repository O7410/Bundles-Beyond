package o7410.bundlesbeyond;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;

//? if fabric
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
//? if >=1.21.10 {
import net.minecraft.client.gui.screens.ChatScreen;
//?}
import net.minecraft.client.InputType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.network.chat.Component;

public class BundlesBeyondCommand {
    public static <T extends SharedSuggestionProvider> void registerCommand(CommandDispatcher<T> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(LiteralArgumentBuilder.<T>literal("bundlesbeyond")
                .executes(BundlesBeyondCommand::configScreen)
                .then(LiteralArgumentBuilder.<T>literal("mod_enabled_state")
                        .executes(BundlesBeyondCommand::executeGetModEnabledState)
                        .then(RequiredArgumentBuilder.<T, ModEnabledState>argument("state", new StringRepresentableArgument<>(ModEnabledState.CODEC, ModEnabledState::values) {})
                                .suggests(BundlesBeyondCommand::getModEnabledStateSuggestions)
                                .executes(BundlesBeyondCommand::executeSetModEnabledState)))
                .then(LiteralArgumentBuilder.<T>literal("scroll_mode")
                        .executes(BundlesBeyondCommand::executeGetScrollMode)
                        .then(RequiredArgumentBuilder.<T, ScrollMode>argument("mode", new StringRepresentableArgument<>(ScrollMode.CODEC, ScrollMode::values) {})
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

    private static void sendFeedback(CommandContext<? extends SharedSuggestionProvider> context, Component text) {
        SharedSuggestionProvider source = context.getSource();

        //? if fabric {
        if (source instanceof FabricClientCommandSource fabricSource) {
            fabricSource.sendFeedback(text);
        }
        //?}
        if (source instanceof CommandSourceStack serverSource) {
            serverSource.sendSuccess(() -> text, false);
        }
    }

    private static void sendError(CommandContext<? extends SharedSuggestionProvider> context, Component text) {
        SharedSuggestionProvider source = context.getSource();
        //? if fabric {
        if (source instanceof FabricClientCommandSource fabricSource) {
            fabricSource.sendError(text);
        }
        //?}
        if (source instanceof CommandSourceStack serverSource) {
            serverSource.sendFailure(text);
        }
    }

    private static int configScreen(CommandContext<? extends SharedSuggestionProvider> context) {
        Minecraft client = Minecraft.getInstance();
        //? if >=1.21.10 {
        if (client.screen instanceof ChatScreen chatScreen) {
            chatScreen.insertText("", true);
        }
        //?}
        client.setLastInputType(InputType.NONE);
        client.setScreen(new BundlesBeyondConfigScreen(null));
        return 0;
    }

    private static int executeGetModEnabledState(CommandContext<? extends SharedSuggestionProvider> context) {
        ModEnabledState state = BundlesBeyondConfig.instance().modEnabledState;
        sendFeedback(context, Component.literal("Bundles Beyond enabled state is currently: ").append(state.getShortNameText()));
        return 0;
    }

    private static CompletableFuture<Suggestions> getModEnabledStateSuggestions(CommandContext<? extends SharedSuggestionProvider> context, SuggestionsBuilder builder) {
        for (ModEnabledState state : ModEnabledState.values()) {
            builder.suggest(state.id, state.getDescriptionText());
        }
        return builder.buildFuture();
    }

    private static int executeSetModEnabledState(CommandContext<? extends SharedSuggestionProvider> context) {
        ModEnabledState newModEnabledState = context.getArgument("state", ModEnabledState.class);
        BundlesBeyondConfig config = BundlesBeyondConfig.instance();
        sendFeedback(context, Component.literal("Bundles Beyond enabled state is now: ").append(newModEnabledState.getShortNameText()));
        if (config.modEnabledState != newModEnabledState) {
            config.modEnabledState = newModEnabledState;
            if (!BundlesBeyondConfig.save()) {
                sendError(context, Component.literal("Failed to save Bundles Beyond config"));
            }
        }
        return 0;
    }

    private static int executeGetScrollMode(CommandContext<? extends SharedSuggestionProvider> context) {
        sendFeedback(context, Component.literal("Scroll mode is currently: ").append(BundlesBeyondConfig.instance().scrollMode.getShortNameText()));
        return 0;
    }

    private static CompletableFuture<Suggestions> getScrollModeSuggestions(CommandContext<? extends SharedSuggestionProvider> context, SuggestionsBuilder builder) {
        for (ScrollMode scrollMode : ScrollMode.values()) {
            builder.suggest(scrollMode.id, scrollMode.getDescriptionText());
        }
        return builder.buildFuture();
    }

    private static int executeSetScrollMode(CommandContext<? extends SharedSuggestionProvider> context) {
        ScrollMode newMode = context.getArgument("mode", ScrollMode.class);
        sendFeedback(context, Component.literal("Scroll mode is now: ").append(newMode.getShortNameText()));
        BundlesBeyondConfig config = BundlesBeyondConfig.instance();
        if (config.scrollMode != newMode) {
            config.scrollMode = newMode;
            if (!BundlesBeyondConfig.save()) {
                sendError(context, Component.literal("Failed to save Bundles Beyond config"));
            }
        }
        return 0;
    }

    private static int executeGetSlotSize(CommandContext<? extends SharedSuggestionProvider> context) {
        int slotSize = BundlesBeyondConfig.instance().slotSize;
        sendFeedback(context, Component.literal("Slot size is currently: " + slotSize + (slotSize == 24 ? " (Vanilla)" : "")));
        return 0;
    }

    private static CompletableFuture<Suggestions> getSlotSizeSuggestions(CommandContext<? extends SharedSuggestionProvider> context, SuggestionsBuilder builder) {
        builder.suggest(24);
        return builder.buildFuture();
    }

    private static int executeSetSlotSize(CommandContext<? extends SharedSuggestionProvider> context) {
        int slotSize = IntegerArgumentType.getInteger(context, "size");
        sendFeedback(context, Component.literal("Slot size is now: " + slotSize + (slotSize == 24 ? " (Vanilla)" : "")));
        BundlesBeyondConfig config = BundlesBeyondConfig.instance();
        if (config.slotSize != slotSize) {
            config.slotSize = slotSize;
            if (!BundlesBeyondConfig.save()) {
                sendError(context, Component.literal("Failed to save Bundles Beyond config"));
            }
        }
        return 0;
    }

    private static int executeReloadConfig(CommandContext<? extends SharedSuggestionProvider> context) {
        if (BundlesBeyondConfig.load()) {
            sendFeedback(context, Component.literal("Reloaded Bundles Beyond config"));
        } else {
            sendError(context, Component.literal("Failed to reload Bundles Beyond config"));
        }
        return 0;
    }
}

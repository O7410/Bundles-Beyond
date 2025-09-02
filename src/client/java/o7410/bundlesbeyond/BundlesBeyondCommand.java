package o7410.bundlesbeyond;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.navigation.GuiNavigationType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

public class BundlesBeyondCommand {
    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(ClientCommandManager.literal("bundlesbeyond")
                .executes(BundlesBeyondCommand::configScreen)
                .then(ClientCommandManager.literal("mod_enabled_state")
                        .executes(BundlesBeyondCommand::executeGetModEnabledState)
                        .then(ClientCommandManager.argument("state", new EnumArgumentType<>(ModEnabledState.CODEC, ModEnabledState::values) {})
                                .suggests(BundlesBeyondCommand::getModEnabledStateSuggestions)
                                .executes(BundlesBeyondCommand::executeSetModEnabledState)))
                .then(ClientCommandManager.literal("scroll_mode")
                        .executes(BundlesBeyondCommand::executeGetScrollMode)
                        .then(ClientCommandManager.argument("mode", new EnumArgumentType<>(ScrollMode.CODEC, ScrollMode::values) {})
                                .suggests(BundlesBeyondCommand::getScrollModeSuggestions)
                                .executes(BundlesBeyondCommand::executeSetScrollMode)
                        )
                )
                .then(ClientCommandManager.literal("slot_size")
                        .executes(BundlesBeyondCommand::executeGetSlotSize)
                        .then(ClientCommandManager.argument("size", IntegerArgumentType.integer(18))
                                .suggests(BundlesBeyondCommand::getSlotSizeSuggestions)
                                .executes(BundlesBeyondCommand::executeSetSlotSize)))
                .then(ClientCommandManager.literal("reloadconfig").executes(BundlesBeyondCommand::executeReloadConfig))
        );
    }

    private static int configScreen(CommandContext<FabricClientCommandSource> context) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.setNavigationType(GuiNavigationType.NONE);
        client.setScreen(new BundlesBeyondConfigScreen(null));
        return 0;
    }

    private static int executeGetModEnabledState(CommandContext<FabricClientCommandSource> context) {
        ModEnabledState state = BundlesBeyondConfig.instance().modEnabledState;
        context.getSource().sendFeedback(Text.literal("Bundles Beyond enabled state is currently: ").append(state.getShortNameText()));
        return 0;
    }

    private static CompletableFuture<Suggestions> getModEnabledStateSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        for (ModEnabledState state : ModEnabledState.values()) {
            builder.suggest(state.id, state.getDescriptionText());
        }
        return builder.buildFuture();
    }

    private static int executeSetModEnabledState(CommandContext<FabricClientCommandSource> context) {
        ModEnabledState newModEnabledState = context.getArgument("state", ModEnabledState.class);
        BundlesBeyondConfig config = BundlesBeyondConfig.instance();
        context.getSource().sendFeedback(Text.literal("Bundles Beyond enabled state is now: ").append(newModEnabledState.getShortNameText()));
        if (config.modEnabledState != newModEnabledState) {
            config.modEnabledState = newModEnabledState;
            if (!BundlesBeyondConfig.save()) {
                context.getSource().sendError(Text.literal("Failed to save Bundles Beyond config"));
            }
        }
        return 0;
    }

    private static int executeGetScrollMode(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.literal("Scroll mode is currently: ").append(BundlesBeyondConfig.instance().scrollMode.getShortNameText()));
        return 0;
    }

    private static CompletableFuture<Suggestions> getScrollModeSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        for (ScrollMode scrollMode : ScrollMode.values()) {
            builder.suggest(scrollMode.id, scrollMode.getDescriptionText());
        }
        return builder.buildFuture();
    }

    private static int executeSetScrollMode(CommandContext<FabricClientCommandSource> context) {
        ScrollMode newMode = context.getArgument("mode", ScrollMode.class);
        context.getSource().sendFeedback(Text.literal("Scroll mode is now: ").append(newMode.getShortNameText()));
        BundlesBeyondConfig config = BundlesBeyondConfig.instance();
        if (config.scrollMode != newMode) {
            config.scrollMode = newMode;
            if (!BundlesBeyondConfig.save()) {
                context.getSource().sendError(Text.literal("Failed to save Bundles Beyond config"));
            }
        }
        return 0;
    }

    private static int executeGetSlotSize(CommandContext<FabricClientCommandSource> context) {
        int slotSize = BundlesBeyondConfig.instance().slotSize;
        context.getSource().sendFeedback(Text.literal("Slot size is currently: " + slotSize + (slotSize == 24 ? " (Vanilla)" : "")));
        return 0;
    }

    private static CompletableFuture<Suggestions> getSlotSizeSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        builder.suggest(24);
        return builder.buildFuture();
    }

    private static int executeSetSlotSize(CommandContext<FabricClientCommandSource> context) {
        int slotSize = IntegerArgumentType.getInteger(context, "size");
        context.getSource().sendFeedback(Text.literal("Slot size is now: " + slotSize + (slotSize == 24 ? " (Vanilla)" : "")));
        BundlesBeyondConfig config = BundlesBeyondConfig.instance();
        if (config.slotSize != slotSize) {
            config.slotSize = slotSize;
            if (!BundlesBeyondConfig.save()) {
                context.getSource().sendError(Text.literal("Failed to save Bundles Beyond config"));
            }
        }
        return 0;
    }

    private static int executeReloadConfig(CommandContext<FabricClientCommandSource> context) {
        if (BundlesBeyondConfig.load()) {
            context.getSource().sendFeedback(Text.literal("Reloaded Bundles Beyond config"));
        } else {
            context.getSource().sendError(Text.literal("Failed to reload Bundles Beyond config"));
        }
        return 0;
    }
}

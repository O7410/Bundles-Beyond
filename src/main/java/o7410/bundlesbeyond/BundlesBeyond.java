package o7410.bundlesbeyond;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.BundleItemSelectedC2SPacket;
//? if fabric {
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//?}
//? if neoforge {
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
*///?}
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
//? if <1.21.10 {
/*import net.minecraft.util.Util;
*///?}
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

//? if neoforge {
/*@Mod(value = BundlesBeyond.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = BundlesBeyond.MOD_ID, value = Dist.CLIENT)
*///?}
public class BundlesBeyond/*? if fabric {*/ implements ClientModInitializer/*?}*/ {

    public static final String MOD_ID = "bundlesbeyond";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    //? if <1.21.10 {
    /*public static final String KEY_CATEGORY = Util.createTranslationKey("category", Identifier.of(MOD_ID, "bundles_beyond"));
    *///?} else if fabric {
    public static final KeyBinding.Category KEY_CATEGORY = KeyBinding.Category.create(Identifier.of(MOD_ID, "bundles_beyond"));
    //?} else if neoforge {
    /*public static final KeyBinding.Category KEY_CATEGORY = new KeyBinding.Category(Identifier.of(MOD_ID, "bundles_beyond"));
    *///?}

    public static final KeyBinding SCROLL_AXIS_KEY = new KeyBinding(
            "key." + MOD_ID + ".scroll_axis",
            InputUtil.UNKNOWN_KEY.getCode(),
            KEY_CATEGORY
    );
    public static final KeyBinding MOD_ENABLED_KEY = new KeyBinding(
            "key." + MOD_ID + ".mod_enabled",
            InputUtil.UNKNOWN_KEY.getCode(),
            KEY_CATEGORY
    );

    public static boolean isModEnabled() {
        return BundlesBeyondConfig.instance().modEnabledState.isEnabled();
    }

    //? if fabric {
    @Override
    public void onInitializeClient() {
    //?} else {
    /*public BundlesBeyond(ModContainer container) {
    *///?}
        BundlesBeyondConfig.load();

        //? if fabric {
        registerKeybinds(KeyBindingHelper::registerKeyBinding);
        ClientCommandRegistrationCallback.EVENT.register(BundlesBeyondCommand::registerCommand);
        //?}

        //? if neoforge {
        /*container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, modListScreen) -> new BundlesBeyondConfigScreen(modListScreen));
        *///?}
    }

    private static void registerKeybinds(Consumer<KeyBinding> register) {
        register.accept(SCROLL_AXIS_KEY);
        register.accept(MOD_ENABLED_KEY);
    }

    //? if neoforge {
    /*@SubscribeEvent
    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        //? if >=1.21.10 {
        /^event.registerCategory(KEY_CATEGORY);
        ^///?}
        registerKeybinds(event::register);
    }
    *///?}

    //? if neoforge {
    /*@SubscribeEvent
    private static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        BundlesBeyondCommand.registerCommand(event.getDispatcher(), event.getBuildContext());
    }
    *///?}

    public static int getKeyCode(KeyBinding keyBinding) {
        //? if fabric {
        return KeyBindingHelper.getBoundKeyOf(keyBinding).getCode();
        //?} else {
        /*return keyBinding.getKey().getCode();
        *///?}
    }

    public static void sendBundleSelectedPacket(int slotId, int selectedIndex) {
        ClientPlayNetworkHandler clientPlayNetworkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (clientPlayNetworkHandler == null) return;
        BundleItemSelectedC2SPacket packet = new BundleItemSelectedC2SPacket(slotId, selectedIndex);
        //? if fabric {
        clientPlayNetworkHandler.sendPacket(packet);
        //?} else {
        /*clientPlayNetworkHandler.send(packet);
        *///?}
    }
}

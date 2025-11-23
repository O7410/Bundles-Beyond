package o7410.bundlesbeyond;

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
//? if <1.21.10 {
/*import net.minecraft.Util;
*///?}
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Consumer;

//? if neoforge {
/*@Mod(value = BundlesBeyond.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = BundlesBeyond.MOD_ID, value = Dist.CLIENT)
*///?}
public class BundlesBeyond/*? if fabric {*/ implements ClientModInitializer/*?}*/ {

    public static final String MOD_ID = "bundlesbeyond";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    //? if <1.21.10 {
    /*public static final String KEY_CATEGORY = Util.makeDescriptionId("category", ResourceLocation.fromNamespaceAndPath(MOD_ID, "bundles_beyond"));
    *///?} else if fabric {
    public static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath(MOD_ID, "bundles_beyond"));
    //?} else if neoforge {
    /*public static final KeyMapping.Category KEY_CATEGORY = new KeyMapping.Category(ResourceLocation.fromNamespaceAndPath(MOD_ID, "bundles_beyond"));
    *///?}

    public static final KeyMapping SCROLL_AXIS_KEY = new KeyMapping(
            "key." + MOD_ID + ".scroll_axis",
            InputConstants.UNKNOWN.getValue(),
            KEY_CATEGORY
    );
    public static final KeyMapping MOD_ENABLED_KEY = new KeyMapping(
            "key." + MOD_ID + ".mod_enabled",
            InputConstants.UNKNOWN.getValue(),
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

    private static void registerKeybinds(Consumer<KeyMapping> register) {
        register.accept(SCROLL_AXIS_KEY);
        register.accept(MOD_ENABLED_KEY);
    }

    //? if neoforge {
    /*@SubscribeEvent
    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        //? if >=1.21.10 {
        event.registerCategory(KEY_CATEGORY);
        //?}
        registerKeybinds(event::register);
    }
    *///?}

    //? if neoforge {
    /*@SubscribeEvent
    private static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        BundlesBeyondCommand.registerCommand(event.getDispatcher(), event.getBuildContext());
    }
    *///?}

    public static int getKeyCode(KeyMapping keyBinding) {
        //? if fabric {
        return KeyBindingHelper.getBoundKeyOf(keyBinding).getValue();
        //?} else {
        /*return keyBinding.getKey().getValue();
        *///?}
    }

    public static void sendBundleSelectedPacket(int slotId, int selectedIndex) {
        ClientPacketListener clientPlayNetworkHandler = Minecraft.getInstance().getConnection();
        if (clientPlayNetworkHandler == null) return;
        ServerboundSelectBundleItemPacket packet = new ServerboundSelectBundleItemPacket(slotId, selectedIndex);
        clientPlayNetworkHandler.send(packet);
    }
}

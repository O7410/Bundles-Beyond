package o7410.bundlesbeyond;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundlesBeyondClient implements ClientModInitializer {

    public static final String MOD_ID = "bundles-beyond";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static KeyBinding scrollAxisKey;
    public static KeyBinding modEnabledKey;

    public static boolean isModEnabled() {
        return BundlesBeyondConfig.instance().modEnabledState.isEnabled();
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

        BundlesBeyondConfig.load();

        ClientCommandRegistrationCallback.EVENT.register(BundlesBeyondCommand::registerCommand);
    }
}

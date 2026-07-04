package kotleni.pickupnotif.client

import kotleni.pickuphud.ModConfig
import kotleni.pickuphud.ui.screens.TrackedItemsScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
import net.minecraft.client.Minecraft
import net.minecraft.client.KeyMapping
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.resources.Identifier
import org.lwjgl.glfw.GLFW

class PickupnotifClient : ClientModInitializer {
    private val client: Minecraft get() = Minecraft.getInstance()
    private val pickupsManager = PickupsManager()
    private val pickupsTracker by lazy { PickupsTracker(client, pickupsManager) }
    private lateinit var openTrackedItemsKeybind: KeyMapping

    override fun onInitializeClient() {
        ModConfig.load()
        openTrackedItemsKeybind = KeyMappingHelper.registerKeyMapping(
            KeyMapping(
                "key.pickuphud.open_tracked_items",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath("pickuphud", "pickuphud")),
            )
        )

        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath("pickuphud", "pickup_hud"),
            HudElement { context, _ ->
                PickupsMessagesRenderer.render(context, client.font, pickupsManager.allPickups, ModConfig.INSTANCE)
            }
        )

        ClientTickEvents.END_CLIENT_TICK.register {
            pickupsTracker.tick()
            while (openTrackedItemsKeybind.consumeClick()) {
                val currentConfigCopy = ModConfig.INSTANCE.copy()
                client.setScreenAndShow(TrackedItemsScreen(null, currentConfigCopy))
            }
        }
    }
}

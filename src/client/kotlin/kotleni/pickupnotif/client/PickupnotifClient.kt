package kotleni.pickupnotif.client

import kotleni.pickuphud.ModConfig
import kotleni.pickupnotif.ExperienceOrbPickupCallback
import kotleni.pickupnotif.ItemPickupCallback
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack

class PickupnotifClient : ClientModInitializer {
    private val client: MinecraftClient get() = MinecraftClient.getInstance();
    private val pickupsManager = PickupsManager();

    override fun onInitializeClient() {
        ModConfig.load()

        // client.player?.on
        HudRenderCallback.EVENT.register { drawContext, tickCounter ->
            PickupsMessagesRenderer.render(drawContext, client.textRenderer, pickupsManager.allPickups)
        }

        ItemPickupCallback.EVENT?.register { player, stack ->
            client.execute {
                onPickupItem(player, stack)
            }
        }

        ExperienceOrbPickupCallback.EVENT?.register { player, experience ->
            client.execute {
                onPickupExperienceOrb(player, experience)
            }
        }
    }

    private fun onPickupItem(player: PlayerEntity, stack: ItemStack) {
        val mainCount = client.player?.inventory
            ?.main
            ?.toList()
            ?.filter { it.item.name == stack.item.name }
            ?.map { it.count }
            ?.reduceOrNull { a, b -> a + b } ?: 0
        val offHandCount = client.player?.inventory
            ?.offHand
            ?.toList()
            ?.filter { it.item.name == stack.item.name }
            ?.map { it.count }
            ?.reduceOrNull { a, b -> a + b } ?: 0
        val totalCount = mainCount + offHandCount

        pickupsManager.addItemPickup(stack, totalCount);
    }

    private fun onPickupExperienceOrb(player: PlayerEntity, experience: Int) {
        val totalCount = player.totalExperience
        pickupsManager.addExperiencePickup(experience, totalCount)
    }
}

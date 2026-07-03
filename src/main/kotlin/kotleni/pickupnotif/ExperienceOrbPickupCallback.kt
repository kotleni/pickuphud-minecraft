package kotleni.pickupnotif

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.world.entity.player.Player

fun interface ExperienceOrbPickupCallback {
    fun onPickup(player: Player, experience: Int)

    companion object {
        @JvmField
        val EVENT: Event<ExperienceOrbPickupCallback> = EventFactory.createArrayBacked(ExperienceOrbPickupCallback::class.java) { listeners ->
            ExperienceOrbPickupCallback { player, experience ->
                for (listener in listeners) {
                    listener.onPickup(player, experience)
                }
            }
        }
    }
}
package kotleni.pickupnotif

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

fun interface ItemPickupCallback {
    fun onPickup(player: Player, stack: ItemStack)

    companion object {
        @JvmField
        val EVENT: Event<ItemPickupCallback> = EventFactory.createArrayBacked(ItemPickupCallback::class.java) { listeners ->
            ItemPickupCallback { player, stack ->
                for (listener in listeners) {
                    listener?.onPickup(player, stack)
                }
            }
        }
    }
}
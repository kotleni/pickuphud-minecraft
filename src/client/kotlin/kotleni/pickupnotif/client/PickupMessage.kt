package kotleni.pickupnotif.client

import net.minecraft.world.item.ItemStack

sealed class PickupMessage(
    var createTime: Long
) {
    data class Item(
        val stack: ItemStack,
        var increaseCount: Int,
        var totalCount: Int,
        val messageCreateTime: Long
    ): PickupMessage(messageCreateTime)

    data class ExperienceOrb(
        var increaseCount: Int,
        var totalCount: Int,
        val messageCreateTime: Long
    ): PickupMessage(messageCreateTime)
}
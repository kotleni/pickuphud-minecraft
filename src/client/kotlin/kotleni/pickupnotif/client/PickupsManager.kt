package kotleni.pickupnotif.client

import kotleni.pickuphud.ModConfig
import net.minecraft.item.ItemStack

class PickupsManager {
    private var pickupMessages: ArrayList<PickupMessage> = arrayListOf();

    val allPickups: List<PickupMessage> get() = pickupMessages

    private fun cleanup() {
        pickupMessages.removeIf { System.currentTimeMillis() - it.createTime >= ModConfig.INSTANCE.messageTime }
    }

    fun addItemPickup(stack: ItemStack, totalItemsOfThisType: Int) {
        cleanup()

        val prevMessage = pickupMessages.filter { it is PickupMessage.Item }
            .find { (it as PickupMessage.Item).stack.item.name == stack.item.name } as? PickupMessage.Item?

        if(prevMessage != null) {
            prevMessage.increaseCount += stack.count
            prevMessage.totalCount = totalItemsOfThisType
            prevMessage.createTime = System.currentTimeMillis()
        } else {
            pickupMessages.add(PickupMessage.Item(
                stack,
                stack.count,
                totalItemsOfThisType,
                System.currentTimeMillis(),
            ))
        }
    }

    fun addExperiencePickup(experience: Int, totalCount: Int) {
        cleanup()

        val prevMessage = pickupMessages.find { it is PickupMessage.ExperienceOrb } as? PickupMessage.ExperienceOrb?

        if(prevMessage != null) {
            prevMessage.increaseCount += experience
            prevMessage.totalCount = totalCount
            prevMessage.createTime = System.currentTimeMillis()
        } else {
            pickupMessages.add(PickupMessage.ExperienceOrb(
                experience,
                totalCount,
                System.currentTimeMillis(),
            ))
        }
    }
}
package kotleni.pickupnotif.client

import kotleni.pickuphud.ModConfig
import net.minecraft.world.item.ItemStack

class PickupsManager {
    private var pickupMessages: ArrayList<PickupMessage> = arrayListOf()

    val allPickups: List<PickupMessage> get() = pickupMessages

    private fun cleanup() {
        pickupMessages.removeIf { System.currentTimeMillis() - it.createTime >= ModConfig.INSTANCE.messageTime }
    }

    fun addItemPickup(stack: ItemStack, totalItemsOfThisType: Int) {
        cleanup()

        var prevMessage: PickupMessage.Item? = null
        for (message in pickupMessages) {
            if (message !is PickupMessage.Item) continue
            if (!ItemStack.isSameItemSameComponents(message.stack, stack)) continue
            prevMessage = message
            break
        }

        if (prevMessage != null) {
            prevMessage.increaseCount += stack.count()
            prevMessage.totalCount = totalItemsOfThisType
            prevMessage.createTime = System.currentTimeMillis()
        } else {
            pickupMessages.add(
                PickupMessage.Item(
                    stack.copy(),
                    stack.count(),
                    totalItemsOfThisType,
                    System.currentTimeMillis(),
                )
            )
        }
    }

    fun addExperiencePickup(experience: Int, totalCount: Int) {
        cleanup()

        val prevMessage = pickupMessages.find { it is PickupMessage.ExperienceOrb } as? PickupMessage.ExperienceOrb?

        if (prevMessage != null) {
            prevMessage.increaseCount += experience
            prevMessage.totalCount = totalCount
            prevMessage.createTime = System.currentTimeMillis()
        } else {
            pickupMessages.add(
                PickupMessage.ExperienceOrb(
                    experience,
                    totalCount,
                    System.currentTimeMillis(),
                )
            )
        }
    }
}

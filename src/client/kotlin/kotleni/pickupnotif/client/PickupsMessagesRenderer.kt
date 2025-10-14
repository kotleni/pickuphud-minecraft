package kotleni.pickupnotif.client

import kotleni.pickuphud.ModConfig
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.util.Colors

object PickupsMessagesRenderer {
    private fun generateLine(message: PickupMessage): String {
        return when(message) {
            is PickupMessage.Item -> "${message.stack.item.name.string} +${message.increaseCount} (${message.totalCount})"
            is PickupMessage.ExperienceOrb -> "Experience +${message.increaseCount} (${message.totalCount})"
        };
    }

    fun render(drawContext: DrawContext, textRenderer: TextRenderer, messages: List<PickupMessage>) {
        var renderedCount = 0
        messages.forEach { message ->
            if(System.currentTimeMillis() - message.createTime > ModConfig.INSTANCE.messageTime) return@forEach

            val line = generateLine(message)
            val margin = 16
            val padding = 6
            val width = textRenderer.getWidth(line)
            val height = textRenderer.fontHeight

            val x = drawContext.scaledWindowWidth - width - padding
            val y = drawContext.scaledWindowHeight - height - (margin * renderedCount) - padding - 6

            if(ModConfig.INSTANCE.isRenderItemIcon)
            when(message) {
                is PickupMessage.Item -> drawContext.drawItem(message.stack, x - 20, y)
                is PickupMessage.ExperienceOrb -> drawContext.drawItem(Items.EXPERIENCE_BOTTLE.defaultStack, x - 20, y)
            }

            drawContext.drawText(
                textRenderer,
                line,
                x,
                y + (height / 2),
                Colors.WHITE,
                false
            )

            renderedCount += 1
        }
    }
}
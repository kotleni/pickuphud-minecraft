package kotleni.pickupnotif.client

import kotleni.pickuphud.ModConfig
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Formatting
import kotlin.math.max

object PickupsMessagesRenderer {
    private fun generateLine(message: PickupMessage): String {
        return when(message) {
            is PickupMessage.Item -> {
                val stacksCount = (message.totalCount.toDouble() / message.stack.item.maxCount).toInt()
                val partialStackCount = message.totalCount - (stacksCount * message.stack.item.maxCount)
                val totalCount = if(ModConfig.INSTANCE.isDisplayTotalCountInStacks && stacksCount > 0)
                    "$stacksCount ${if(stacksCount == 1) "stack" else "stacks"}${if(partialStackCount == 0) "" else " + $partialStackCount"}"
                else
                    message.totalCount.toString()

                return "${message.stack.itemName.string} +${message.increaseCount} ($totalCount)"
            }
            is PickupMessage.ExperienceOrb -> "Experience +${message.increaseCount} (${message.totalCount})"
        }
    }

    private fun getRarityFormatting(message: PickupMessage): Formatting {
        return when(message) {
            is PickupMessage.Item -> message.stack.rarity.formatting
            is PickupMessage.ExperienceOrb -> Formatting.WHITE
        } ?: Formatting.WHITE
    }

    fun render(drawContext: DrawContext, textRenderer: TextRenderer, messages: List<PickupMessage>) {
        var yOffset = 0
        var renderedCount = 0
        val screenMargin = 4 // A small margin from the screen edges

        messages.forEach { message ->
            if (System.currentTimeMillis() - message.createTime > ModConfig.INSTANCE.messageTime) return@forEach

            if (renderedCount >= ModConfig.INSTANCE.maxMessagesOnScreen) {
                message.createTime = System.currentTimeMillis()
                return@forEach
            }

            // --- Configuration ---
            val padding = ModConfig.INSTANCE.messagePadding
            val gap = ModConfig.INSTANCE.gapBetweenMessages
            val textColor = Colors.WHITE
            val backgroundColor = Colors.BLACK
            val renderIcon = ModConfig.INSTANCE.isRenderItemIcon
            val iconScale = 0.75f

            // --- Dimensions ---
            val line = generateLine(message)
            val textWidth = textRenderer.getWidth(line)
            val textHeight = textRenderer.fontHeight

            val baseItemSize = 16 // The item's original size
            val scaledItemSize = (baseItemSize * iconScale).toInt() // The new, smaller size

            val iconTextGap = 4
            val iconAreaWidth = if (renderIcon) scaledItemSize + iconTextGap else 0

            val contentHeight = max(textHeight, if (renderIcon) scaledItemSize else 0)
            val backgroundHeight = contentHeight + (padding * 2)
            val backgroundWidth = textWidth + iconAreaWidth + (padding * 2)

            // --- Positions ---
            val backgroundX = drawContext.scaledWindowWidth - backgroundWidth - screenMargin
            val backgroundY = drawContext.scaledWindowHeight - backgroundHeight - yOffset - screenMargin

            // Draw background
            drawContext.fill(backgroundX, backgroundY, backgroundX + backgroundWidth, backgroundY + backgroundHeight, backgroundColor)

            // --- Draw Icon with Scaling ---
            if (renderIcon) {
                val iconX = backgroundX + padding
                val iconY = backgroundY + (backgroundHeight / 2) - (scaledItemSize / 2)

                val matrices = drawContext.matrices
                matrices.push() // Save the current matrix state

                // We need to translate to the icon's position, scale, and then draw at (0,0)
                matrices.translate(iconX.toFloat(), iconY.toFloat(), 0f)
                matrices.scale(iconScale, iconScale, 1f)

                // Draw the item at the new, scaled-down origin
                when (message) {
                    is PickupMessage.Item -> drawContext.drawItem(message.stack, 0, 0)
                    is PickupMessage.ExperienceOrb -> drawContext.drawItem(Items.EXPERIENCE_BOTTLE.defaultStack, 0, 0)
                }

                matrices.pop() // Restore the matrix to its original state
            }

            // --- Draw Text ---
            val text = if(ModConfig.INSTANCE.isColorizeTextByRarity)
                Text.literal(line).formatted(getRarityFormatting(message))
            else
                Text.literal(line)

            val textX = backgroundX + padding + iconAreaWidth
            val textY = backgroundY + (backgroundHeight / 2) - (textHeight / 2)

            drawContext.drawText(
                textRenderer,
                text,
                textX,
                textY,
                textColor,
                true // shadow
            )

            // Update offset
            yOffset += backgroundHeight + gap
            renderedCount++
        }
    }
}
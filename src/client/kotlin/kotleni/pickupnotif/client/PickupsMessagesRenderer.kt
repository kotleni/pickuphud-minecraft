package kotleni.pickupnotif.client

import kotleni.pickuphud.ModConfig
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.world.item.Items
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import kotlin.math.max

object PickupsMessagesRenderer {
    private fun colorInt(red: Int, green: Int, blue: Int, alpha: Int): Int {
        val a = alpha.coerceIn(0, 255)
        val r = red.coerceIn(0, 255)
        val g = green.coerceIn(0, 255)
        val b = blue.coerceIn(0, 255)
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    private fun generateLine(message: PickupMessage, config: ModConfig): String {
        return when (message) {
            is PickupMessage.Item -> {
                val stacksCount = (message.totalCount.toDouble() / message.stack.item.defaultMaxStackSize).toInt()
                val partialStackCount = message.totalCount - (stacksCount * message.stack.item.defaultMaxStackSize)
                val totalCount = if (config.isDisplayTotalCountInStacks && stacksCount > 0) {
                    "$stacksCount ${if (stacksCount == 1) "stack" else "stacks"}${if (partialStackCount == 0) "" else " + $partialStackCount"}"
                } else {
                    message.totalCount.toString()
                }

                "${message.stack.hoverName.string} +${message.increaseCount} ($totalCount)"
            }

            is PickupMessage.ExperienceOrb -> "Experience +${message.increaseCount} (${message.totalCount})"
        }
    }

    private fun getRarityFormatting(message: PickupMessage): ChatFormatting {
        return when (message) {
            is PickupMessage.Item -> message.stack.rarity.color()
            is PickupMessage.ExperienceOrb -> ChatFormatting.AQUA
        } ?: ChatFormatting.WHITE
    }

    fun render(
        context: GuiGraphicsExtractor,
        font: Font,
        messages: List<PickupMessage>,
        config: ModConfig = ModConfig.INSTANCE,
    ) {
        var yOffset = 0
        var renderedCount = 0

        for (message in messages) {
            if (System.currentTimeMillis() - message.createTime > config.messageTime) continue

            if (renderedCount >= config.maxMessagesOnScreen) {
                message.createTime = System.currentTimeMillis()
                continue
            }

            val padding = config.messagePadding
            val gap = config.gapBetweenMessages

            val textColor = -1
            val backgroundColor = colorInt(
                config.messageBackgroundShade,
                config.messageBackgroundShade,
                config.messageBackgroundShade,
                config.messageBackgroundOpacity,
            )
            val textBackgroundColor = colorInt(0, 0, 0, config.textBackgroundOpacity)

            val renderIcon = config.isRenderItemIcon
            val iconScale = 0.75f

            val line = generateLine(message, config)
            val textWidth = font.width(line)
            val textHeight = font.lineHeight

            val baseItemSize = 16
            val scaledItemSize = (baseItemSize * iconScale).toInt()
            val iconTextGap = 4
            val iconAreaWidth = if (renderIcon) scaledItemSize + iconTextGap else 0

            val contentHeight = max(textHeight, if (renderIcon) scaledItemSize else 0)
            val backgroundHeight = contentHeight + (padding * 2)
            val backgroundWidth = textWidth + iconAreaWidth + (padding * 2)

            val maxX = max(context.guiWidth() - backgroundWidth, 0)
            val maxY = max(context.guiHeight() - backgroundHeight, 0)

            val horizontalOffset = max(config.hudOffsetX, 0)
            val verticalOffset = max(config.hudOffsetY, 0)

            val rawX = if (config.isHudRightAligned) {
                context.guiWidth() - backgroundWidth - horizontalOffset
            } else {
                horizontalOffset
            }

            val rawY = if (config.isHudBottomAligned) {
                context.guiHeight() - backgroundHeight - verticalOffset - yOffset
            } else {
                verticalOffset + yOffset
            }

            val backgroundX = rawX.coerceIn(0, maxX)
            val backgroundY = rawY.coerceIn(0, maxY)

            if (config.messageBackgroundOpacity > 0) {
                context.fill(
                    backgroundX,
                    backgroundY,
                    backgroundX + backgroundWidth,
                    backgroundY + backgroundHeight,
                    backgroundColor,
                )
            }

            if (renderIcon) {
                val iconX = backgroundX + padding
                val iconY = backgroundY + (backgroundHeight / 2) - (scaledItemSize / 2)
                val pose = context.pose()

                pose.pushMatrix()
                try {
                    pose.translate(iconX.toFloat(), iconY.toFloat())
                    pose.scale(iconScale, iconScale)

                    when (message) {
                        is PickupMessage.Item -> context.item(message.stack, 0, 0)
                        is PickupMessage.ExperienceOrb -> context.item(Items.EXPERIENCE_BOTTLE.defaultInstance, 0, 0)
                    }
                } finally {
                    pose.popMatrix()
                }
            }

            val text = if (config.isColorizeTextByRarity) {
                Component.literal(line).withStyle(getRarityFormatting(message))
            } else {
                Component.literal(line)
            }

            val textX = backgroundX + padding + iconAreaWidth
            val textY = backgroundY + (backgroundHeight / 2) - (textHeight / 2)

            if (config.textBackgroundOpacity > 0) {
                context.fill(textX - 1, textY - 1, textX + textWidth + 1, textY + textHeight + 1, textBackgroundColor)
            }

            context.text(font, text, textX, textY, textColor, true)

            yOffset += backgroundHeight + gap
            renderedCount++
        }
    }
}

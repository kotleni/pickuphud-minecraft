package kotleni.pickuphud.ui.screens

import kotleni.pickuphud.ModConfig
import kotleni.pickupnotif.client.PickupMessage
import kotleni.pickupnotif.client.PickupsMessagesRenderer
import kotleni.pickuphud.settings.ModSetting
import kotleni.pickuphud.settings.ModSettingValue
import kotleni.pickuphud.settings.behaviorSettings
import kotleni.pickuphud.settings.renderingSettings
import kotleni.pickuphud.ui.widgets.IntSliderWidget
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.CycleButton
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphicsExtractor // 26.1 rename
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.narration.NarratableEntry

class ModSettingsScreen(private val parent: Screen?) : Screen(Component.literal("")) {
    private data class SettingsPage(
        val title: String,
        val settings: List<ModSetting<out Any>>,
    )

    private val pages = listOf(
        SettingsPage("Rendering", renderingSettings),
        SettingsPage("Behavior", behaviorSettings),
    )

    private var currentPageIndex = 0
    private val modConfigCopy: ModConfig = ModConfig.INSTANCE.copy()

    private var yOffset = 0

    private fun buildPreviewMessages(now: Long): List<PickupMessage> {
        val messages = mutableListOf<PickupMessage>(
            PickupMessage.Item(ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1), 1, 3, now),
            PickupMessage.Item(ItemStack(Items.COBBLESTONE, 32), 32, 128, now),
        )

        if (modConfigCopy.isDisplayExperienceOrb) {
            messages.add(PickupMessage.ExperienceOrb(9, 429, now))
        }

        return messages
    }

    private fun switchPage(delta: Int) {
        val lastIndex = pages.lastIndex
        currentPageIndex = (currentPageIndex + delta + pages.size) % pages.size
        currentPageIndex = currentPageIndex.coerceIn(0, lastIndex)
        rebuildWidgets()
    }

    private fun addSettingToggle(setting: ModSetting<Boolean>) {
        val rowY = 54 + yOffset

        addRenderableWidget(StringWidget(
            this.width / 2 - 155,
            rowY,
            150,
            20,
            Component.literal(setting.title),
            font,
        ))

        val toggleButton = CycleButton.onOffBuilder(setting.getValue(modConfigCopy))
            .create(
                this.width / 2 + 5,
                rowY,
                150,
                20,
                Component.literal(setting.title)
            ) { _, value: Boolean ->
                setting.setValue(modConfigCopy, value)
            }

        addRenderableWidget(toggleButton)
        yOffset += 24
    }

    private fun addSettingIntField(setting: ModSetting<Int>) {
        val intValue = setting.value as ModSettingValue.ValueInt
        val rowY = 54 + yOffset

        addRenderableWidget(StringWidget(
            this.width / 2 - 155,
            rowY,
            150,
            20,
            Component.literal(setting.title),
            font,
        ))

        val slider = IntSliderWidget(
            this.width / 2 + 5,
            rowY,
            150,
            20,
            Component.literal(setting.title),
            setting.getValue(modConfigCopy),
            intValue.min,
            intValue.max,
            onChangeValue = { newValue ->
                setting.setValue(modConfigCopy, newValue)
            },
        )

        addRenderableWidget(slider)
        yOffset += 24
    }

    @Suppress("UNCHECKED_CAST")
    private fun addSettingItem(setting: ModSetting<out Any>) {
        when (setting.value) {
            is ModSettingValue.ValueBoolean -> addSettingToggle(setting as ModSetting<Boolean>)
            is ModSettingValue.ValueInt -> addSettingIntField(setting as ModSetting<Int>)
            is ModSettingValue.ValueString -> {}
        }
    }

    override fun init() {
        yOffset = 0

        addRenderableWidget(
            Button.builder(Component.literal("<")) { _ ->
                switchPage(-1)
            }
                .bounds(this.width / 2 - 90, 26, 20, 20)
                .build()
        )

        addRenderableWidget(
            Button.builder(Component.literal(">")) { _ ->
                switchPage(1)
            }
                .bounds(this.width / 2 + 70, 26, 20, 20)
                .build()
        )

        pages[currentPageIndex].settings.forEach { setting ->
            addSettingItem(setting)
        }

        addRenderableWidget(
            Button.builder(Component.literal("Tracked Items...")) { _ ->
                minecraft?.setScreen(TrackedItemsScreen(this as Screen, modConfigCopy))
            }
                .bounds(this.width / 2 - 60, this.height - 56, 120, 20)
                .build()
        )

        addRenderableWidget(
            Button.builder(Component.literal("Cancel")) { _ ->
                onClose()
            }
                .bounds(this.width / 2 - 205, this.height - 28, 200, 20)
                .build()
        )

        addRenderableWidget(
            Button.builder(Component.literal("Save & Quit")) { _ ->
                ModConfig.INSTANCE.apply(modConfigCopy)
                ModConfig.save()
                onClose()
            }
                .bounds(this.width / 2 + 5, this.height - 28, 200, 20)
                .build()
        )
    }

    override fun render(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, deltaTracker: DeltaTracker) {
        val now = System.currentTimeMillis()
        val previewMessages = buildPreviewMessages(now)

        // Ensure PickupsMessagesRenderer is also updated to take GuiGraphicsExtractor
        PickupsMessagesRenderer.render(context, font, previewMessages, modConfigCopy)

        super.render(context, mouseX, mouseY, deltaTracker)

        context.centeredText(
            font,
            Component.literal("Pickup HUD Configuration"),
            this.width / 2,
            8,
            0xFFFFFF,
        )

        context.centeredText(
            font,
            Component.literal("Page ${currentPageIndex + 1}/${pages.size} - ${pages[currentPageIndex].title}"),
            this.width / 2,
            32,
            0xFFFFFF,
        )

        context.centeredText(
            font,
            Component.literal("Tip: Change 'Open Tracked Items' in Controls -> Key Binds -> Pickup HUD"),
            this.width / 2,
            this.height - 68,
            0xD6D6D6,
        )
    }

    override fun onClose() {
        minecraft?.setScreen(parent)
    }
}
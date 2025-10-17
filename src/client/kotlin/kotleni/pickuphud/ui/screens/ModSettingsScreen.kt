package kotleni.pickuphud.ui.screens

import kotleni.pickuphud.ModConfig
import kotleni.pickuphud.settings.ModSetting
import kotleni.pickuphud.settings.ModSettingValue
import kotleni.pickuphud.settings.behaviorSettings
import kotleni.pickuphud.settings.renderingSettings
import kotleni.pickuphud.ui.widgets.IntSliderWidget
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.CyclingButtonWidget
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.text.Text
import kotlin.Boolean

class ModSettingsScreen(private val parent: Screen?) : Screen(Text.literal("")) {
    private val modConfigCopy: ModConfig = ModConfig.INSTANCE.copy()

    private var yOffset = 0

    private fun addSettingToggle(setting: ModSetting<Boolean>) {
        addDrawableChild(TextWidget(
            this.width / 2 - 155,
            (this.height / 6) + yOffset,
            150,
            20,
            Text.literal(setting.title),
            textRenderer
        ))
        addDrawableChild(
            CyclingButtonWidget.onOffBuilder(Text.literal("Enable"), Text.literal("Disable"))
                .initially(setting.getValue(modConfigCopy))
                ?.omitKeyText()
                ?.build(
                    this.width / 2 + 5,
                    (this.height / 6) + yOffset,
                    150,
                    20,
                    null
                ) { button: CyclingButtonWidget<Boolean?>?, value: Boolean ->
                    setting.setValue(modConfigCopy, value)
                }
        )

        yOffset += 24
    }

    private fun addSettingIntField(setting: ModSetting<Int>) {
        val intValue = setting.value as ModSettingValue.ValueInt

        addDrawableChild(TextWidget(
            this.width / 2 - 155,
            (this.height / 6) + yOffset,
            150,
            20,
            Text.literal(setting.title),
            textRenderer
        ))
        addDrawableChild(
            IntSliderWidget(
                this.width / 2 + 5,
                (this.height / 6) + yOffset,
                150,
                20,
                Text.literal(setting.title),
                setting.getValue(modConfigCopy),
                intValue.min,
                intValue.max,
                onChangeValue = { newValue ->
                    setting.setValue(modConfigCopy, newValue)
                }
            )
        )
        yOffset += 24
    }

    private fun <T> addSettingItem(setting: ModSetting<T>) {
        when(setting.value) {
            is ModSettingValue.ValueBoolean -> {
                addSettingToggle(setting as ModSetting<Boolean>)
            }
            is ModSettingValue.ValueInt -> {
                addSettingIntField(setting as ModSetting<Int>)
            }
        }
    }

    override fun init() {
        yOffset = 0

        // Title
        addDrawableChild(TextWidget(
            12, // FIXME: Weird paddings
            4,
            200,
            32,
            Text.literal("Pickup HUD Configuration"),
            textRenderer
        ))

        renderingSettings.forEach { setting ->
            addSettingItem(setting)
        }
        behaviorSettings.forEach { setting ->
            addSettingItem(setting)
        }

        // Done btn
        addDrawableChild(
            ButtonWidget.Builder(
                Text.literal("Cancel")
            ) {
                close()
            }
                .dimensions(this.width / 2 - 205, this.height - 28, 200, 20)
                .build()
        )

        addDrawableChild(
            ButtonWidget.Builder(
                Text.literal("Save & Quit")
            ) {
                ModConfig.INSTANCE.apply(modConfigCopy)
                ModConfig.save()

                close()
            }.dimensions(this.width / 2 + 5, this.height - 28, 200, 20).build()
        )
    }

    override fun close() {
        client?.setScreen(parent)
    }
}
package kotleni.pickuphud.ui.widgets

import net.minecraft.client.gui.components.AbstractSliderButton
import net.minecraft.network.chat.Component

class IntSliderWidget(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val text: Component,
    private val initialValue: Int,
    private val min: Int,
    private val max: Int,
    private val onChangeValue: (value: Int) -> Unit = { }
) : AbstractSliderButton(x, y, width, height, text, (initialValue - min).toDouble() / (max - min)) {
    val actualValue: Int get() = (value * (max - min)).toInt() + min

    init {
        updateMessage()
    }

    override fun updateMessage() {
        this.message = Component.literal("Value: $actualValue")
    }

    override fun applyValue() {
        onChangeValue(actualValue)
    }
}

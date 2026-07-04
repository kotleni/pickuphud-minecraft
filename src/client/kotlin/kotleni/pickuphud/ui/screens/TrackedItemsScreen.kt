package kotleni.pickuphud.ui.screens

import kotleni.pickuphud.ModConfig
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.world.item.ItemStack
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier

class TrackedItemsScreen(
    private val parent: Screen?,
    private val config: ModConfig,
) : Screen(Component.literal("Tracked Items")) {
    private val allItemIds: List<Identifier> = BuiltInRegistries.ITEM.keySet().toList().sortedBy { it.toString() }
    private val filteredResults = mutableListOf<Identifier>()
    private val trackedIds = linkedSetOf<Identifier>()

    private var selectedId: Identifier? = null
    private var searchQuery: String = ""
    private var scrollOffset = 0
    private var searchField: EditBox? = null

    private val listX: Int get() = width / 2 - 205
    private val listY: Int get() = 66
    private val listWidth: Int get() = 410
    private val rowHeight: Int get() = 24
    private val listBottom: Int get() = height - 84
    private val visibleRows: Int get() = maxOf(1, (listBottom - listY) / rowHeight)

    override fun init() {
        if (trackedIds.isEmpty()) {
            config.trackedItemIdsCsv
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .forEach { raw ->
                    val id = Identifier.tryParse(raw) ?: return@forEach
                    if (BuiltInRegistries.ITEM.containsKey(id)) trackedIds.add(id)
                }
        }

        refreshFiltered()

        val field = EditBox(font, listX, 40, listWidth, 20, Component.literal("Search items"))
        field.setMaxLength(128)
        field.setValue(searchQuery)
        field.setResponder { value ->
            searchQuery = value
            refreshFiltered()
        }
        addRenderableWidget(field)
        searchField = field

        addRenderableWidget(
            Button.builder(Component.literal("Toggle Selected")) {
                selectedId?.let {
                    toggleTracked(it)
                }
            }
                .bounds(width / 2 - 205, height - 52, 200, 20)
                .build()
        )
        addRenderableWidget(
            Button.builder(Component.literal("Clear Tracked")) {
                trackedIds.clear()
                syncToConfig()
            }
                .bounds(width / 2 + 5, height - 52, 200, 20)
                .build()
        )
        addRenderableWidget(
            Button.builder(Component.literal("Back")) { onClose() }
                .bounds(width / 2 - 205, height - 28, 410, 20)
                .build()
        )
    }

    private fun refreshFiltered() {
        filteredResults.clear()
        if (searchQuery.isBlank()) {
            filteredResults.addAll(allItemIds)
        } else {
            val query = searchQuery.lowercase()
            filteredResults.addAll(allItemIds.filter { id ->
                val item = BuiltInRegistries.ITEM.getValue(id)
                val itemName = item.defaultInstance.hoverName.string.lowercase()
                id.toString().contains(query) || itemName.contains(query)
            })
        }

        filteredResults.sortWith(
            compareByDescending<Identifier> { trackedIds.contains(it) }
                .thenBy { BuiltInRegistries.ITEM.getValue(it).defaultInstance.hoverName.string.lowercase() }
                .thenBy { it.toString() }
        )

        val maxOffset = maxOf(0, filteredResults.size - visibleRows)
        scrollOffset = scrollOffset.coerceIn(0, maxOffset)
    }

    private fun syncToConfig() {
        config.trackedItemIdsCsv = trackedIds.joinToString(",") { it.toString() }
    }

    private fun toggleTracked(id: Identifier) {
        if (!trackedIds.remove(id)) trackedIds.add(id)
        syncToConfig()
    }

    private fun ensureTracked(id: Identifier) {
        if (trackedIds.add(id)) {
            syncToConfig()
        }
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        if (event.button() == 0) {
            val mx = event.x()
            val my = event.y()
            if (mx in listX.toDouble()..(listX + listWidth).toDouble() && my >= listY && my < listBottom) {
                val idx = ((my - listY) / rowHeight).toInt()
                val itemIdx = scrollOffset + idx
                if (itemIdx < filteredResults.size) {
                    val id = filteredResults[itemIdx]
                    selectedId = id
                    ensureTracked(id)
                    return true
                }
            }
        }
        return super.mouseClicked(event, doubleClick)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (mouseX < listX || mouseX > listX + listWidth || mouseY < listY || mouseY > listBottom) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
        }

        val maxOffset = maxOf(0, filteredResults.size - visibleRows)
        val prev = scrollOffset
        val step = if (verticalAmount < 0) 1 else if (verticalAmount > 0) -1 else 0
        scrollOffset = (scrollOffset + step).coerceIn(0, maxOffset)
        return prev != scrollOffset
    }

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.extractRenderState(context, mouseX, mouseY, partialTick)

        val visible = filteredResults.drop(scrollOffset).take(visibleRows)
        visible.forEachIndexed { idx, id ->
            val y = listY + idx * rowHeight
            val item = BuiltInRegistries.ITEM.getValue(id)
            val isTracked = trackedIds.contains(id)
            val isSelected = selectedId == id
            val bg = when {
                isSelected -> 0xAA2979FF.toInt()
                isTracked -> 0xAA235C2E.toInt()
                else -> 0x88303030.toInt()
            }

            context.fill(listX, y, listX + listWidth, y + rowHeight - 1, bg)
            context.item(ItemStack(item), listX + 4, y + 2)

            context.text(font, item.defaultInstance.hoverName, listX + 24, y + 4, 0xFFFFFFFF.toInt())
            context.text(font, Component.literal(id.toString()), listX + 24, y + 14, 0xFFA8A8A8.toInt())
            context.text(
                font,
                Component.literal(if (isTracked) "TOGGLED" else "UNTOGGLED"),
                listX + listWidth - 88,
                y + 8,
                if (isTracked) 0xFFA7FFA7.toInt() else 0xFFFFB7B7.toInt(),
                false,
            )
        }

        context.centeredText(font, Component.literal("Tracked Items"), width / 2, 16, 0xFFFFFFFF.toInt())
        context.centeredText(
            font,
            Component.literal("Tracked Items: ${trackedIds.size}"),
            width / 2,
            height - 74,
            0xFFB0F0B0.toInt(),
        )
        context.centeredText(
            font,
            Component.literal("Click any item row to track item"),
            width / 2,
            height - 62,
            0xFFCFCFCF.toInt(),
        )
    }

    override fun onClose() {
        if (parent == null) {
            ModConfig.save()
        }
        parent?.let { minecraft.setScreenAndShow(it) }
    }
}

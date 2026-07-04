package kotleni.pickupnotif.client

import kotleni.pickuphud.ModConfig
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.sounds.SoundEvents
import net.minecraft.resources.Identifier

class PickupsTracker(
    private val client: Minecraft,
    private val pickupsManager: PickupsManager,
) {
    private val scanIntervalTicks = 3

    private var isInitialized = false
    private var previousItemCounts = hashMapOf<Item, Int>()
    private var currentItemCounts = hashMapOf<Item, Int>()

    private var previousExperience = 0
    private var lastProcessedPlayerAge = -1

    private var cachedTrackedCsv = ""
    private var cachedTrackedItems: Set<Item>? = null

    private fun collectInventoryCounts(player: Player): HashMap<Item, Int> {
        currentItemCounts.clear()

        val inventory = player.inventory
        for (slot in 0 until inventory.containerSize) {
            val stack = inventory.getItem(slot)
            if (stack.isEmpty) continue

            val item = stack.item
            currentItemCounts[item] = (currentItemCounts[item] ?: 0) + stack.count
        }

        return currentItemCounts
    }

    fun reset() {
        isInitialized = false
        previousItemCounts.clear()
        currentItemCounts.clear()
        previousExperience = 0
        lastProcessedPlayerAge = -1
    }

    fun tick() {
        val player = client.player ?: run {
            reset()
            return
        }

        if (player.tickCount == lastProcessedPlayerAge) return
        if (player.tickCount % scanIntervalTicks != 0) return
        lastProcessedPlayerAge = player.tickCount

        val currentCounts = collectInventoryCounts(player)
        val currentExperience = player.totalExperience

        if (!isInitialized) {
            swapBuffers()
            previousExperience = currentExperience
            isInitialized = true
            return
        }

        val cfg = ModConfig.INSTANCE
        val needsTrackedResolution = cfg.isShowOnlyTrackedItems || cfg.isPlaySoundOnTrackedItem
        val trackedItems = if (needsTrackedResolution) getTrackedItemsCached(cfg.trackedItemIdsCsv) else null

        for ((item, currentCount) in currentCounts) {
            val previousCount = previousItemCounts[item] ?: 0
            val delta = currentCount - previousCount
            if (delta <= 0) continue

            val isTrackedItem = trackedItems?.contains(item) == true
            val shouldShowPickup = !cfg.isShowOnlyTrackedItems || trackedItems == null || isTrackedItem

            if (shouldShowPickup) {
                pickupsManager.addItemPickup(ItemStack(item, delta), currentCount)
            }

            if (isTrackedItem && cfg.isPlaySoundOnTrackedItem) {
                val volume = (cfg.pickupSoundVolume.coerceIn(0, 100) / 100f) * 1.45f
                if (volume > 0f) {
                    player.playSound(SoundEvents.PLAYER_LEVELUP, volume.coerceAtMost(1.7f), 1.8f)
                    player.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), volume.coerceAtMost(1.7f), 1.95f)
                }
            }
        }

        val experienceDelta = currentExperience - previousExperience
        if (cfg.isDisplayExperienceOrb && experienceDelta > 0) {
            pickupsManager.addExperiencePickup(experienceDelta, currentExperience)
        }

        swapBuffers()
        previousExperience = currentExperience
    }

    private fun swapBuffers() {
        val previous = previousItemCounts
        previousItemCounts = currentItemCounts
        currentItemCounts = previous
        currentItemCounts.clear()
    }

    private fun getTrackedItemsCached(csv: String): Set<Item>? {
        if (csv == cachedTrackedCsv) return cachedTrackedItems

        cachedTrackedCsv = csv
        cachedTrackedItems = parseTrackedItems(csv)
        return cachedTrackedItems
    }

    private fun parseTrackedItems(csv: String): Set<Item>? {
        val ids = csv.split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (ids.isEmpty()) return null

        val result = linkedSetOf<Item>()

        for (raw in ids) {
            val identifier = Identifier.tryParse(raw) ?: continue
            if (!BuiltInRegistries.ITEM.containsKey(identifier)) continue
            result.add(BuiltInRegistries.ITEM.getValue(identifier))
        }

        return if (result.isEmpty()) null else result
    }
}

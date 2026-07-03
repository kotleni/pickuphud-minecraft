package kotleni.pickuphud

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import kotleni.pickuphud.ui.screens.ModSettingsScreen
import net.minecraft.client.gui.screens.Screen

class ModMenuApiImpl : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<Screen> {
        return ConfigScreenFactory { parent ->
            return@ConfigScreenFactory ModSettingsScreen(parent)
        }
    }
}
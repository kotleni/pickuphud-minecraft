package kotleni.pickupnotif.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import kotleni.pickupnotif.ItemPickupCallback;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Inject(
            method = "playerTouch", at = @At(
            value = "HEAD", target = "Lnet/minecraft/entity/player/PlayerEntity;increaseStat(Lnet/minecraft/stat/Stat;I)V"
    )
    )
    public void onPlayerPickup(Player player, CallbackInfo ci, @Local ItemStack stack) {
        if (player instanceof ServerPlayer serverPlayer && !stack.isEmpty()) {
            ItemPickupCallback.EVENT.invoker().onPickup(serverPlayer, stack);
        }
    }
}
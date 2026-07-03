package kotleni.pickupnotif.mixin;

import kotleni.pickupnotif.ExperienceOrbPickupCallback;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbEntityMixin {
    @Inject(
            method = "playerTouch", at = @At(
            value = "HEAD", target = "Lnet/minecraft/entity/player/PlayerEntity;sendPickup(Lnet/minecraft/entity/Entity;I)V"
    )
    )
    private void onPickup(Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer) {
            ExperienceOrb self = (ExperienceOrb) (Object) this;
            ExperienceOrbPickupCallback.EVENT.invoker().onPickup(serverPlayer, self.getValue());
        }
    }
}

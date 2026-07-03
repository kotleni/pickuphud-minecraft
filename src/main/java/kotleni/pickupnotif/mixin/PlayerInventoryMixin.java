package kotleni.pickupnotif.mixin;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class PlayerInventoryMixin {
    @Inject(
            method = "add(Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At("HEAD")
    )
    private void insertStack(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) { }
}

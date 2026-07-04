package kotleni.pickupnotif.mixin.client;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderPass.RenderArea;
import com.mojang.blaze3d.systems.RenderPassBackend;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPass.class)
public class RenderPassScissorFixMixin {
    @Shadow
    @Final
    private RenderArea renderArea;

    @Shadow
    @Final
    private RenderPassBackend backend;

    @Inject(method = "enableScissor(IIII)V", at = @At("HEAD"), cancellable = true)
    private void clampScissor(int left, int top, int width, int height, CallbackInfo ci) {
        if (width <= 0 || height <= 0) {
            return;
        }

        int renderRight = this.renderArea.x() + this.renderArea.width();
        int renderBottom = this.renderArea.y() + this.renderArea.height();

        int clampedLeft = Math.max(left, this.renderArea.x());
        int clampedTop = Math.max(top, this.renderArea.y());
        int clampedWidth = Math.min(width, renderRight - clampedLeft);
        int clampedHeight = Math.min(height, renderBottom - clampedTop);

        if (clampedWidth <= 0 || clampedHeight <= 0) {
            ci.cancel();
            return;
        }

        if (clampedLeft != left || clampedTop != top || clampedWidth != width || clampedHeight != height) {
            this.backend.enableScissor(clampedLeft, clampedTop, clampedWidth, clampedHeight);
            ci.cancel();
        }
    }
}

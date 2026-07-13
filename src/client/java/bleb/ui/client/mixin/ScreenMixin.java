package bleb.ui.client.mixin;

import bleb.ui.client.BackgroundManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {
    @Shadow public int width;
    @Shadow public int height;

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void drawGlobalCustomBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        BackgroundManager.tickAnimation();

        if (BackgroundManager.isLoaded) {
            RenderSystem.setShaderTexture(0, BackgroundManager.BACKGROUND_TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            guiGraphics.blit(BackgroundManager.BACKGROUND_TEXTURE, 0, 0, 0, 0, this.width, this.height, this.width, this.height);

            ci.cancel();
        }
    }
}

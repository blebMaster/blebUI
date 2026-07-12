package bleb.ui.client;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.SliderComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class BackgroundConfigScreen extends BaseOwoScreen<FlowLayout> {
    private final Screen parent;
    private LabelComponent themeNameLabel;
    private FlowLayout previewBox; // Вынесли рамку в поле класса, чтобы иметь к ней доступ из любого метода

    private final int previewWidth = 200;
    private final int previewHeight = 130;
    private int temporaryFps = BackgroundManager.targetFps;

    public BackgroundConfigScreen(Screen parent) {
        this.parent = parent;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, (width, height) ->
                (FlowLayout) Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100))
                        .horizontalAlignment(HorizontalAlignment.CENTER)
                        .verticalAlignment(VerticalAlignment.CENTER)
        );
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        LabelComponent titleLabel = Components.label(Component.literal("Background Selection"));
        titleLabel.shadow(true).margins(Insets.bottom(15));
        rootComponent.child(titleLabel);


        FlowLayout selectorRow = (FlowLayout) Containers.horizontalFlow(Sizing.content(), Sizing.content())
                .verticalAlignment(VerticalAlignment.CENTER);

        LabelComponent prevBtn = Components.label(Component.literal("◀ Previous"));
        setupInteractiveText(prevBtn, () -> {
            BackgroundManager.switchBrowsingTheme(false); // Листаем папки назад
            updateThemeUI();
        });
        prevBtn.margins(Insets.right(25));


        previewBox = (FlowLayout) Containers.verticalFlow(Sizing.fixed(previewWidth), Sizing.fixed(previewHeight))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);
        previewBox.surface(Surface.outline(0xFFFFFFFF)); // Белая рамка

        LabelComponent nextBtn = Components.label(Component.literal("Next ▶"));
        setupInteractiveText(nextBtn, () -> {
            BackgroundManager.switchBrowsingTheme(true); // Листаем папки вперед
            updateThemeUI();
        });
        nextBtn.margins(Insets.left(25));

        selectorRow.child(prevBtn).child(previewBox).child(nextBtn);
        rootComponent.child(selectorRow);


        themeNameLabel = Components.label(Component.literal(BackgroundManager.getBrowsingThemeName().toUpperCase()));
        themeNameLabel.shadow(true).margins(Insets.top(8).withBottom(10));
        rootComponent.child(themeNameLabel);


        LabelComponent applyBtn = Components.label(Component.literal("[ APPLY ]"));
        setupInteractiveText(applyBtn, () -> {

            BackgroundManager.applySelectedTheme();


            BackgroundManager.targetFps = this.temporaryFps;
        });
        applyBtn.color(Color.ofRgb(0x40E0D0));
        applyBtn.margins(Insets.bottom(25));
        rootComponent.child(applyBtn);



        SliderComponent fpsSlider = Components.slider(Sizing.fixed(150));


        double initialProgress = (double) (BackgroundManager.targetFps - 1) / (60 - 1);
        fpsSlider.value(initialProgress);
        fpsSlider.message(value -> Component.literal("Frame Rate: " + this.temporaryFps));

        fpsSlider.onChanged().subscribe(newValue -> {
            int scaledFps = 1 + (int) Math.round(newValue * (60 - 1));
            this.temporaryFps = scaledFps;


            fpsSlider.message(value -> Component.literal("Frame Rate: " + scaledFps));
        });
        rootComponent.child(fpsSlider);


        LabelComponent backBtn = Components.label(Component.literal("◀ Back"));
        setupInteractiveText(backBtn, () -> Minecraft.getInstance().setScreen(this.parent));
        backBtn.positioning(Positioning.absolute(20, this.height - 30));
        rootComponent.child(backBtn);

        refreshPreviewImage();
    }

    private void refreshPreviewImage() {
        if (previewBox == null) return;

        previewBox.clearChildren();

        if (BackgroundManager.isPreviewLoaded) {
            previewBox.child(Components.texture(
                    BackgroundManager.PREVIEW_TEXTURE, 0, 0, previewWidth, previewHeight, previewWidth, previewHeight
            ));
        }
    }

    private void updateThemeUI() {
        if (themeNameLabel != null) {
            themeNameLabel.text(Component.literal(BackgroundManager.getBrowsingThemeName().toUpperCase()));
        }
        refreshPreviewImage();
    }

    private void setupInteractiveText(LabelComponent label, Runnable onClick) {
        label.shadow(true).cursorStyle(CursorStyle.HAND);
        label.mouseDown().subscribe((mouseX, mouseY, button) -> {
            onClick.run();
            return true;
        });
        label.mouseEnter().subscribe(() -> label.color(Color.ofRgb(0x40E0D0)));
        label.mouseLeave().subscribe(() -> {
            if (label.text().getString().contains("APPLY")) {
                label.color(Color.ofRgb(0x40E0D0));
            } else {
                label.color(Color.ofRgb(0xFFFFFF));
            }
        });
    }

    @Override
    public void renderBackground(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        BackgroundManager.tickAnimation();

        if (BackgroundManager.isLoaded) {
            com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, BackgroundManager.BACKGROUND_TEXTURE);
            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            guiGraphics.blit(BackgroundManager.BACKGROUND_TEXTURE, 0, 0, 0, 0, this.width, this.height, this.width, this.height);
        } else {
            this.renderTransparentBackground(guiGraphics);
        }
    }

    @Override
    public void removed() {
        super.removed();
        BackgroundManager.saveConfig();
    }
}

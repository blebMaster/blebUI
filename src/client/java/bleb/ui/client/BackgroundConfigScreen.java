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
    private FlowLayout previewBox;

    private LabelComponent titleLabel;
    private LabelComponent prevBtn;
    private LabelComponent nextBtn;
    private LabelComponent applyBtn;
    private LabelComponent backBtn;
    private SliderComponent fpsSlider;

    private final int previewWidth = 200;
    private final int previewHeight = 130;
    private int temporaryFps = BackgroundManager.targetFps;

    private long lastLanguageCheckTime = 0;
    private String lastLanguageCode = "";

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
        titleLabel = Components.label(Component.literal(""));
        titleLabel.shadow(true).margins(Insets.bottom(15));
        rootComponent.child(titleLabel);

        FlowLayout selectorRow = (FlowLayout) Containers.horizontalFlow(Sizing.content(), Sizing.content())
                .verticalAlignment(VerticalAlignment.CENTER);

        prevBtn = Components.label(Component.literal(""));
        setupInteractiveText(prevBtn, () -> {
            BackgroundManager.switchBrowsingTheme(false);
            updateThemeUI();
        });
        prevBtn.margins(Insets.right(25));

        previewBox = (FlowLayout) Containers.verticalFlow(Sizing.fixed(previewWidth), Sizing.fixed(previewHeight))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);
        previewBox.surface(Surface.outline(0xFFFFFFFF));

        nextBtn = Components.label(Component.literal(""));
        setupInteractiveText(nextBtn, () -> {
            BackgroundManager.switchBrowsingTheme(true);
            updateThemeUI();
        });
        nextBtn.margins(Insets.left(25));

        selectorRow.child(prevBtn).child(previewBox).child(nextBtn);
        rootComponent.child(selectorRow);

        themeNameLabel = Components.label(Component.literal(BackgroundManager.getBrowsingThemeName().toUpperCase()));
        themeNameLabel.shadow(true).margins(Insets.top(8).withBottom(10));
        rootComponent.child(themeNameLabel);

        applyBtn = Components.label(Component.literal(""));
        setupInteractiveText(applyBtn, () -> {
            BackgroundManager.applySelectedTheme();
            BackgroundManager.targetFps = this.temporaryFps;
        });
        applyBtn.margins(Insets.bottom(25));
        rootComponent.child(applyBtn);

        fpsSlider = Components.slider(Sizing.fixed(150));
        double initialProgress = (double) (BackgroundManager.targetFps - 1) / (60 - 1);
        fpsSlider.value(initialProgress);

        fpsSlider.onChanged().subscribe(newValue -> {
            int scaledFps = 1 + (int) Math.round(newValue * (60 - 1));
            this.temporaryFps = scaledFps;
            boolean isRussian = Minecraft.getInstance().options.languageCode.equalsIgnoreCase("ru_ru");
            fpsSlider.message(value -> Component.literal((isRussian ? "Скорость кадров: " : "Frame Rate: ") + scaledFps));
        });
        rootComponent.child(fpsSlider);

        backBtn = Components.label(Component.literal(""));
        setupInteractiveText(backBtn, () -> Minecraft.getInstance().setScreen(this.parent));
        backBtn.positioning(Positioning.absolute(20, this.height - 30));
        rootComponent.child(backBtn);

        refreshPreviewImage();
        this.lastLanguageCode = Minecraft.getInstance().options.languageCode;
        updateTranslations();
    }

    private void updateTranslations() {
        boolean isRussian = this.lastLanguageCode.equalsIgnoreCase("ru_ru");

        if (titleLabel != null) titleLabel.text(Component.literal(isRussian ? "Настройка фонов" : "Background Selection"));
        if (prevBtn != null) prevBtn.text(Component.literal(isRussian ? "◀ Назад" : "◀ Previous"));
        if (nextBtn != null) nextBtn.text(Component.literal(isRussian ? "Вперед ▶" : "Next ▶"));
        if (applyBtn != null) applyBtn.text(Component.literal(isRussian ? "[ ПРИМЕНИТЬ ]" : "[ APPLY ]"));
        if (backBtn != null) backBtn.text(Component.literal(isRussian ? "◀ Назад" : "◀ Back"));
        if (fpsSlider != null) fpsSlider.message(value -> Component.literal((isRussian ? "Скорость кадров: " : "Frame Rate: ") + this.temporaryFps));
        if (applyBtn != null) applyBtn.color(Color.ofRgb(0x40E0D0));
    }

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastLanguageCheckTime >= 3000) {
            String currentLang = Minecraft.getInstance().options.languageCode;
            if (!currentLang.equalsIgnoreCase(this.lastLanguageCode)) {
                this.lastLanguageCode = currentLang;
                updateTranslations();
                System.out.println("[BlebUI] Экран настроек зафиксировал смену языка на " + currentLang);
            }
            lastLanguageCheckTime = currentTime;
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
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
            String text = label.text().getString();
            if (text.contains("APPLY") || text.contains("ПРИМЕНИТЬ")) {
                label.color(Color.ofRgb(0x40E0D0));
            } else {
                label.color(Color.ofRgb(0xFFFFFF));
            }
        });
    }

    @Override
    public void removed() {
        super.removed();
        BackgroundManager.saveConfig();
    }
    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
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
}

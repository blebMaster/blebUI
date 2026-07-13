package bleb.ui.client;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class CustomPauseScreen extends BaseOwoScreen<FlowLayout> {
    private LabelComponent configBtn;
    private LabelComponent resumeBtn;
    private LabelComponent optionsBtn;
    private LabelComponent resourcesBtn;
    private LabelComponent lanBtn;
    private LabelComponent disconnectBtn;

    private LabelComponent titleLabel;
    private long lastLanguageCheckTime = 0;
    private String lastLanguageCode = "";

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, (width, height) ->
                (FlowLayout) Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100))
                        .horizontalAlignment(HorizontalAlignment.CENTER)
                        .verticalAlignment(VerticalAlignment.TOP)
        );
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        FlowLayout topBar = (FlowLayout) Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(30))
                .verticalAlignment(VerticalAlignment.CENTER)
                .horizontalAlignment(HorizontalAlignment.LEFT);

        topBar.surface(Surface.flat(0x55000000));
        topBar.padding(Insets.horizontal(15));

        resumeBtn = createCustomButton("", true, () ->
                Minecraft.getInstance().setScreen(null) // Закрывает меню паузы
        );

        optionsBtn = createCustomButton("", true, () ->
                Minecraft.getInstance().setScreen(new OptionsScreen(this, Minecraft.getInstance().options))
        );

        configBtn = createCustomButton("", true, () ->
                Minecraft.getInstance().setScreen(new BackgroundConfigScreen(this))
        );

        resourcesBtn = createCustomButton("", true, () -> {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new PackSelectionScreen(mc.getResourcePackRepository(), repository -> {
                mc.options.updateResourcePacks(repository);
                mc.setScreen(this);
            }, mc.getResourcePackDirectory(), Component.translatable("resourcePack.title")));
        });

        Minecraft mc = Minecraft.getInstance();
        boolean isSingleplayer = mc.hasSingleplayerServer();

        lanBtn = createCustomButton("", isSingleplayer, () -> {
            try {
                net.minecraft.client.gui.screens.Screen lanScreen =
                        (net.minecraft.client.gui.screens.Screen) Class.forName("net.minecraft.client.gui.screens.ShareToLanScreen")
                                .getConstructor(net.minecraft.client.gui.screens.Screen.class)
                                .newInstance(this);
                Minecraft.getInstance().setScreen(lanScreen);
            } catch (Exception e) {
                Minecraft.getInstance().setScreen(new net.minecraft.client.gui.screens.PauseScreen(true));
            }
        });

        disconnectBtn = createCustomButton("", true, () -> {
            if (mc.level != null) {
                mc.level.disconnect();
            }
            mc.disconnect(new CustomMenuScreen());
        });


        topBar.child(resumeBtn.margins(Insets.horizontal(10)));
        topBar.child(optionsBtn.margins(Insets.horizontal(10)));
        topBar.child(configBtn.margins(Insets.horizontal(10)));
        topBar.child(lanBtn.margins(Insets.horizontal(10)));
        topBar.child(resourcesBtn.margins(Insets.horizontal(10)));
        topBar.child(disconnectBtn.margins(Insets.horizontal(10)));

        titleLabel = Components.label(Component.literal(""));
        titleLabel.shadow(true).color(Color.ofRgb(0xaaaaaa));
        titleLabel.positioning(Positioning.absolute(12, this.height - 20));

        rootComponent.child(topBar);
        rootComponent.child(titleLabel);

        this.lastLanguageCode = Minecraft.getInstance().options.languageCode;
        updateButtonTranslations();
    }

    private void updateButtonTranslations() {
        boolean isRussian = this.lastLanguageCode.equalsIgnoreCase("ru_ru");

        if (resumeBtn != null) resumeBtn.text(Component.literal(isRussian ? "Вернуться к игре" : "Back to Game"));
        if (optionsBtn != null) optionsBtn.text(Component.literal(isRussian ? "Настройки" : "Options"));
        if (resourcesBtn != null) resourcesBtn.text(Component.literal(isRussian ? "Наборы ресурсов" : "Resource Packs"));
        if (configBtn != null) configBtn.text(Component.literal(isRussian ? "Настройки фона" : "Background Settings"));
        if (lanBtn != null) lanBtn.text(Component.literal(isRussian ? "Открыть для сети" : "Open to LAN"));
        if (disconnectBtn != null) disconnectBtn.text(Component.literal(isRussian ? "Выйти" : "Disconnect"));
        if (titleLabel != null) titleLabel.text(Component.literal(isRussian ? "Режим паузы" : "Game Menu"));
    }

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLanguageCheckTime >= 3000) {
            String currentLang = Minecraft.getInstance().options.languageCode;
            if (!currentLang.equalsIgnoreCase(this.lastLanguageCode)) {
                this.lastLanguageCode = currentLang;
                updateButtonTranslations();
            }
            lastLanguageCheckTime = currentTime;
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private LabelComponent createCustomButton(String text, boolean enabled, Runnable onClick) {
        LabelComponent label = Components.label(Component.literal(text));

        if (!enabled) {
            label.shadow(true).color(Color.ofRgb(0x555555));
            return label;
        }

        label.shadow(true).cursorStyle(CursorStyle.HAND);

        label.mouseDown().subscribe((mouseX, mouseY, button) -> {
            onClick.run();
            return true;
        });
        label.mouseEnter().subscribe(() -> label.color(Color.ofRgb(0x40E0D0)));
        label.mouseLeave().subscribe(() -> label.color(Color.ofRgb(0xFFFFFF)));
        return label;
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

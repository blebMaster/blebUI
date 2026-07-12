package bleb.ui.client;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class CustomMenuScreen extends BaseOwoScreen<FlowLayout> {
    private LabelComponent singleplayerBtn;
    private LabelComponent multiplayerBtn;
    private LabelComponent modsBtn;
    private LabelComponent optionsBtn;
    private LabelComponent configBtn;
    private LabelComponent quitBtn;
    private LabelComponent titleLabel;
    private LabelComponent versionLabel;

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

        singleplayerBtn = createCustomButton("", () -> {
            try {
                net.minecraft.client.gui.screens.Screen spScreen =
                        (net.minecraft.client.gui.screens.Screen) Class.forName("net.minecraft.client.gui.screens.worldselection.SelectWorldScreen")
                                .getConstructor(net.minecraft.client.gui.screens.Screen.class)
                                .newInstance(this);
                Minecraft.getInstance().setScreen(spScreen);
            } catch (Exception e) {
                System.out.println("[BlebUI] Ошибка: " + e.getMessage());
            }
        });

        multiplayerBtn = createCustomButton("", () -> {
            try {
                net.minecraft.client.gui.screens.Screen mpScreen =
                        (net.minecraft.client.gui.screens.Screen) Class.forName("net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen")
                                .getConstructor(net.minecraft.client.gui.screens.Screen.class)
                                .newInstance(this);
                Minecraft.getInstance().setScreen(mpScreen);
            } catch (Exception e) {
                System.out.println("[BlebUI] Ошибка: " + e.getMessage());
            }
        });

        modsBtn = createCustomButton("", () -> {
            try {
                net.minecraft.client.gui.screens.Screen modsScreen =
                        (net.minecraft.client.gui.screens.Screen) Class.forName("com.terraformersmc.modmenu.gui.ModsScreen")
                                .getConstructor(net.minecraft.client.gui.screens.Screen.class)
                                .newInstance(this);
                Minecraft.getInstance().setScreen(modsScreen);
            } catch (Throwable e) {
                System.out.println("[BlebUI] Ошибка: " + e.getMessage());
            }
        });

        optionsBtn = createCustomButton("", () ->
                Minecraft.getInstance().setScreen(new OptionsScreen(this, Minecraft.getInstance().options))
        );

        configBtn = createCustomButton("", () ->
                Minecraft.getInstance().setScreen(new BackgroundConfigScreen(this))
        );

        quitBtn = createCustomButton("", () ->
                Minecraft.getInstance().close()
        );

        topBar.child(singleplayerBtn.margins(Insets.horizontal(10)));
        topBar.child(multiplayerBtn.margins(Insets.horizontal(10)));
        topBar.child(modsBtn.margins(Insets.horizontal(10)));
        topBar.child(optionsBtn.margins(Insets.horizontal(10)));
        topBar.child(configBtn.margins(Insets.horizontal(10)));
        topBar.child(quitBtn.margins(Insets.horizontal(10)));

        titleLabel = Components.label(Component.literal("BlebikUI"));
        titleLabel.shadow(true).color(Color.ofRgb(0xaaaaaa));
        titleLabel.positioning(Positioning.absolute(12, this.height - 20));

        versionLabel = Components.label(Component.literal("v1.0.0"));
        versionLabel.shadow(true).color(Color.ofRgb(0x555555));
        versionLabel.positioning(Positioning.absolute(this.width - 45, this.height - 20));

        rootComponent.child(topBar);
        rootComponent.child(titleLabel);
        rootComponent.child(versionLabel);

        this.lastLanguageCode = Minecraft.getInstance().options.languageCode;
        updateButtonTranslations();
    }

    private void updateButtonTranslations() {
        boolean isRussian = this.lastLanguageCode.equalsIgnoreCase("ru_ru");

        if (singleplayerBtn != null) singleplayerBtn.text(Component.literal(isRussian ? "Одиночная игра" : "Singleplayer"));
        if (multiplayerBtn != null) multiplayerBtn.text(Component.literal(isRussian ? "Сетевая игра" : "Multiplayer"));
        if (modsBtn != null) modsBtn.text(Component.literal(isRussian ? "Моды" : "Mods"));
        if (optionsBtn != null) optionsBtn.text(Component.literal(isRussian ? "Настройки" : "Options"));
        if (configBtn != null) configBtn.text(Component.literal(isRussian ? "Настройки фона" : "Background Settings"));
        if (quitBtn != null) quitBtn.text(Component.literal(isRussian ? "Выход" : "Quit game"));
    }

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLanguageCheckTime >= 3000) {
            String currentLang = Minecraft.getInstance().options.languageCode;
            if (!currentLang.equalsIgnoreCase(this.lastLanguageCode)) {
                this.lastLanguageCode = currentLang;
                updateButtonTranslations();
                System.out.println("[BlebUI] Язык изменился на " + currentLang + ". Текст главного меню обновлен.");
            }
            lastLanguageCheckTime = currentTime;
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private LabelComponent createCustomButton(String text, Runnable onClick) {
        LabelComponent label = Components.label(Component.literal(text));
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
    protected void init() {
        super.init();
        BackgroundManager.scanThemes();
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

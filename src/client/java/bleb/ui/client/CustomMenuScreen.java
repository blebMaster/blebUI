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

        LabelComponent singleplayerBtn = createCustomButton("Одиночная игра", () -> {
            try {
                net.minecraft.client.gui.screens.Screen spScreen =
                        (net.minecraft.client.gui.screens.Screen) Class.forName("net.minecraft.client.gui.screens.worldselection.SelectWorldScreen")
                                .getConstructor(net.minecraft.client.gui.screens.Screen.class)
                                .newInstance(this);
                Minecraft.getInstance().setScreen(spScreen);
            } catch (Exception e) {
                System.out.println("[BlebUI] Ошибка открытия Одиночной игры: " + e.getMessage());
            }
        });

        LabelComponent multiplayerBtn = createCustomButton("Сетевая игра", () -> {
            try {
                net.minecraft.client.gui.screens.Screen mpScreen =
                        (net.minecraft.client.gui.screens.Screen) Class.forName("net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen")
                                .getConstructor(net.minecraft.client.gui.screens.Screen.class)
                                .newInstance(this);
                Minecraft.getInstance().setScreen(mpScreen);
            } catch (Exception e) {
                System.out.println("[BlebUI] Ошибка открытия Сетевой игры: " + e.getMessage());
            }
        });

        LabelComponent modsBtn = createCustomButton("Моды", () -> {
            try {
                net.minecraft.client.gui.screens.Screen modsScreen =
                        (net.minecraft.client.gui.screens.Screen) Class.forName("com.terraformersmc.modmenu.gui.ModsScreen")
                                .getConstructor(net.minecraft.client.gui.screens.Screen.class)
                                .newInstance(this);
                Minecraft.getInstance().setScreen(modsScreen);
            } catch (Throwable e) {
                System.out.println("[BlebUI] Не удалось запустить ModsScreen: " + e.getMessage());
            }
        });

        LabelComponent optionsBtn = createCustomButton("Настройки", () ->
                Minecraft.getInstance().setScreen(new OptionsScreen(this, Minecraft.getInstance().options))
        );

        LabelComponent configBtn = createCustomButton("Настройки фона", () ->
                Minecraft.getInstance().setScreen(new BackgroundConfigScreen(this))
        );

        LabelComponent quitBtn = createCustomButton("Выход", () ->
                Minecraft.getInstance().close()
        );

        topBar.child(singleplayerBtn.margins(Insets.horizontal(10)));
        topBar.child(multiplayerBtn.margins(Insets.horizontal(10)));
        topBar.child(modsBtn.margins(Insets.horizontal(10)));
        topBar.child(optionsBtn.margins(Insets.horizontal(10)));
        topBar.child(configBtn.margins(Insets.horizontal(10)));
        topBar.child(quitBtn.margins(Insets.horizontal(10)));

        LabelComponent titleLabel = Components.label(Component.literal("BlebikUI"));
        titleLabel.shadow(true).color(Color.ofRgb(0xaaaaaa));

        titleLabel.positioning(Positioning.absolute(12, this.height - 20));

        LabelComponent versionLabel = Components.label(Component.literal("v1.0.1"));
        versionLabel.shadow(true).color(Color.ofRgb(0x555555));

        versionLabel.positioning(Positioning.absolute(this.width - 45, this.height - 20));

        rootComponent.child(topBar);
        rootComponent.child(titleLabel);
        rootComponent.child(versionLabel);
    }



    private LabelComponent createCustomButton(String text, Runnable onClick) {
        LabelComponent label = Components.label(Component.literal(text));
        label.shadow(true);
        label.cursorStyle(CursorStyle.HAND);

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

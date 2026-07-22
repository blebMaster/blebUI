package bleb.ui.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class BackgroundManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("blebui-background");

    public static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("blebui", "dynamic_background");
    public static final ResourceLocation PREVIEW_TEXTURE = ResourceLocation.fromNamespaceAndPath("blebui", "preview_background");

    private static DynamicTexture currentTexture = null;
    private static DynamicTexture previewTexture = null;

    public static List<File> themes = new ArrayList<>();
    public static int activeThemeIndex = 0;
    public static int browsingThemeIndex = 0;
    private static File[] animationFrames = null;
    private static int currentFrameIndex = 0;
    private static long lastFrameTime = 0;

    public static int targetFps = 5;
    public static boolean isLoaded = false;
    public static boolean isPreviewLoaded = false;

    // Считывает файл конфигурации
    private static void loadConfig() {
        File gameDir = Minecraft.getInstance().gameDirectory;
        File configFile = new File(new File(gameDir, "backgrounds"), "config.txt");
        if (!configFile.exists()) return;

        try {
            List<String> lines = Files.readAllLines(configFile.toPath(), StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.startsWith("fps=")) {
                    targetFps = Math.max(1, Math.min(60, Integer.parseInt(line.substring(4).trim())));
                } else if (line.startsWith("theme=")) {
                    String savedTheme = line.substring(6).trim();
                    for (int i = 0; i < themes.size(); i++) {
                        if (themes.get(i).getName().equalsIgnoreCase(savedTheme)) {
                            activeThemeIndex = i;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Не удалось прочитать config.txt", e);
        }
    }

    public static void saveConfig() {
        File gameDir = Minecraft.getInstance().gameDirectory;
        File configFile = new File(new File(gameDir, "backgrounds"), "config.txt");
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8))) {
            writer.println("fps=" + targetFps);
            if (!themes.isEmpty() && activeThemeIndex >= 0 && activeThemeIndex < themes.size()) {
                writer.println("theme=" + themes.get(activeThemeIndex).getName());
            }
            LOGGER.info("Настройки BlebUI успешно сохранены в файл");
        } catch (Exception e) {
            LOGGER.error("Не удалось записать config.txt", e);
        }
    }

    public static void scanThemes() {
        File gameDir = Minecraft.getInstance().gameDirectory;
        File mainFolder = new File(gameDir, "backgrounds");

        File[] subFolders = mainFolder.listFiles(File::isDirectory);
        themes.clear();

        if (subFolders != null && subFolders.length > 0) {
            themes.addAll(Arrays.asList(subFolders));
            themes.sort(Comparator.comparing(File::getName));
        }

        if (!themes.isEmpty()) {
            loadConfig();
            browsingThemeIndex = activeThemeIndex;
            loadActiveTheme(activeThemeIndex);
            loadPreviewFrame();
        } else {
            isLoaded = false;
            isPreviewLoaded = false;
            animationFrames = null;
        }
    }

    public static void loadActiveTheme(int index) {
        if (themes.isEmpty() || index < 0 || index >= themes.size()) return;

        activeThemeIndex = index;
        File selectedThemeFolder = themes.get(index);
        File[] files = selectedThemeFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));

        if (files == null || files.length == 0) {
            animationFrames = null;
            isLoaded = false;
            return;
        }

        Arrays.sort(files, (f1, f2) -> {
            try {
                int num1 = Integer.parseInt(f1.getName().toLowerCase().replace(".png", ""));
                int num2 = Integer.parseInt(f2.getName().toLowerCase().replace(".png", ""));
                return Integer.compare(num1, num2);
            } catch (NumberFormatException e) {
                return f1.getName().compareTo(f2.getName());
            }
        });
        animationFrames = files;
        currentFrameIndex = 0;
        lastFrameTime = System.currentTimeMillis();

        loadFrame(animationFrames[currentFrameIndex]);
    }

    public static void loadPreviewFrame() {
        if (themes.isEmpty() || browsingThemeIndex < 0 || browsingThemeIndex >= themes.size()) return;

        File browsingFolder = themes.get(browsingThemeIndex);
        File[] files = browsingFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));

        if (files == null || files.length == 0) {
            isPreviewLoaded = false;
            return;
        }

        Arrays.sort(files, (f1, f2) -> {
            try {
                int num1 = Integer.parseInt(f1.getName().toLowerCase().replace(".png", ""));
                int num2 = Integer.parseInt(f2.getName().toLowerCase().replace(".png", ""));
                return Integer.compare(num1, num2);
            } catch (NumberFormatException e) {
                return f1.getName().compareTo(f2.getName());
            }
        });
        File firstFrame = files[0];

        try (InputStream stream = new FileInputStream(firstFrame)) {
            NativeImage nativeImage = NativeImage.read(stream);

            if (previewTexture != null) {
                Minecraft.getInstance().getTextureManager().release(PREVIEW_TEXTURE);
            }

            previewTexture = new DynamicTexture(nativeImage);
            Minecraft.getInstance().getTextureManager().register(PREVIEW_TEXTURE, previewTexture);
            isPreviewLoaded = true;
        } catch (IOException e) {
            LOGGER.error("Ошибка загрузки превью кадра: " + firstFrame.getName(), e);
            isPreviewLoaded = false;
        }
    }

    public static void applySelectedTheme() {
        loadActiveTheme(browsingThemeIndex);
        saveConfig();
    }

    public static void switchBrowsingTheme(boolean next) {
        if (themes.isEmpty()) return;

        if (next) {
            browsingThemeIndex = (browsingThemeIndex + 1) % themes.size();
        } else {
            browsingThemeIndex = (browsingThemeIndex - 1 + themes.size()) % themes.size();
        }

        loadPreviewFrame();
    }

    public static String getBrowsingThemeName() {
        if (themes.isEmpty() || browsingThemeIndex < 0 || browsingThemeIndex >= themes.size()) {
            return "Нет тем";
        }
        return themes.get(browsingThemeIndex).getName();
    }

    public static void tickAnimation() {
        if (animationFrames == null || animationFrames.length <= 1) return;

        long currentTime = System.currentTimeMillis();
        long frameDelay = 1000 / targetFps;

        if (currentTime - lastFrameTime >= frameDelay) {
            currentFrameIndex = (currentFrameIndex + 1) % animationFrames.length;
            loadFrame(animationFrames[currentFrameIndex]);
            lastFrameTime = currentTime;
        }
    }

    private static void loadFrame(File file) {
        try (InputStream stream = new FileInputStream(file)) {
            NativeImage nativeImage = NativeImage.read(stream);

            if (currentTexture != null) {
                Minecraft.getInstance().getTextureManager().release(BACKGROUND_TEXTURE);
            }

            currentTexture = new DynamicTexture(nativeImage);
            Minecraft.getInstance().getTextureManager().register(BACKGROUND_TEXTURE, currentTexture);
            isLoaded = true;
        } catch (IOException e) {
            LOGGER.error("Ошибка при чтении кадра: " + file.getName(), e);
        }
    }
}

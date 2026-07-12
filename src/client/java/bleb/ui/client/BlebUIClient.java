package bleb.ui.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BlebUIClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("blebui-client");

	private static final byte[] BLACK_SQUARE_PNG = new byte[]{
			(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
			0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x08, 0x00, 0x00, 0x00, 0x00, 0x3A, 0x7E, (byte) 0x9B,
			0x55, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54, 0x78, (byte) 0x9C, 0x63, 0x60, 0x00, 0x00, 0x00,
			0x02, 0x00, 0x01, 0x73, 0x75, 0x01, 0x18, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE,
			0x42, 0x60, (byte) 0x82
	};

	@Override
	public void onInitializeClient() {
		LOGGER.info("Запуск клиентской инициализации интерфейса...");
		File gameDir = Minecraft.getInstance().gameDirectory;
		File mainFolder = new File(gameDir, "backgrounds");
		if (!mainFolder.exists()) {
			mainFolder.mkdirs();
		}
		File[] existingFolders = mainFolder.listFiles(File::isDirectory);

		if (existingFolders == null || existingFolders.length == 0) {
			File defaultThemeFolder = new File(mainFolder, "Default_Theme");
			if (defaultThemeFolder.mkdirs()) {
				LOGGER.info("Папка 'backgrounds' пуста Создана тема по умолчанию Default_Theme");
				createDefaultTexture(defaultThemeFolder);
			}
		} else {
			LOGGER.info("В папке 'backgrounds' уже найдены темы пользователя");
		}
	}



	private void createDefaultTexture(File folder) {
		File defaultFile = new File(folder, "01_default.png");
		try (FileOutputStream fos = new FileOutputStream(defaultFile)) {
			fos.write(BLACK_SQUARE_PNG);
			LOGGER.info("Тестовый файл создан.");
		} catch (IOException e) {
			LOGGER.error("Не удалось записать тестовый PNG", e);
		}
	}
}

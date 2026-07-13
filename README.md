# BlebUI

## Setup

# BlebikUI 🚀

Fabric mod for customizing your Minecraft 1.21.1 main menu. It replaces the default panorama with a top-bar interface and support for dynamic animated or static backgrounds.

---

## 🛠️ Setup Instruction (How to Use)

After installing the mod and launching the game for the first time, a special folder called `backgrounds` will be automatically created in your main Minecraft directory (where your `mods` folder is located).

### 🏞️ 1. How to Add a Static Background (Single Photo)
1. Go to your Minecraft folder ➡️ open the `backgrounds` folder.
2. Create a new subfolder inside it and give it any name you like (e.g., `MyStaticTheme`).
3. Place **exactly one** high-quality image inside that subfolder.
4. **Important Requirements:**
   * The image **must** be in **`.png`** format (do not just rename a `.jpg` or `.webp` file, convert it properly via Paint or any editor).
   * Keep the filename clean (e.g., `background.png`).

### 🎞️ 2. How to Create a Background Animation (Multiple Photos)
BlebikUI treats folders inside `backgrounds` as individual theme packs. To make a beautiful moving animation:
1. Inside the `backgrounds` folder, create a new subfolder for your animation (e.g., `AnimeTheme`).
2. Extract your video or GIF into separate frame-by-frame images using any online converter.
3. Put all those frames into your created subfolder.
4. **Crucial Naming Rule:**
   Name your frames numerically starting from `1` **without skipping numbers**. To avoid lexicographical sorting bugs in Java
   * `01.png`
   * `02.png`
   * ...
   * `09.png`
   * `10.png`
   * `11.png`

### To create such a sequence of photos from a video, I can recommend https://batchtools.pro/en/videoframes

---

## 🎮 In-Game Controls

1. Launch Minecraft and click the **"Настройки фона" (Background Selection)** button on the top bar.
2. Use the **`◀ Previous`** and **`Next ▶`** text buttons to flip through your theme folders.
3. The central rectangle frame will instantly show you a live preview of the first frame (`01.png`) of that folder.
4. Drag the **Frame Rate** slider to change the animation speed (from **1 to 60 FPS**).
5. Click **`[ APPLY ]`** to instantly change the global background and save your configuration. Your choice and FPS settings will be saved to `config.txt` and remembered forever!

---

## 🧱 Requirements
* Minecraft `1.21.1`
* Fabric Loader
* Fabric API
* Mod Menu

## License

The mod use a MIT License

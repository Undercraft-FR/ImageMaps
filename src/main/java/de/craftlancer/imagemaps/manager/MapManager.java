package de.craftlancer.imagemaps.manager;

import de.craftlancer.imagemaps.ImageMaps;
import de.craftlancer.imagemaps.image.ImageMap;
import de.craftlancer.imagemaps.image.ImageMapRenderer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.server.v1_7_R4.MinecraftServer.getServer;
import static sun.font.FontUtilities.getLogger;

public class MapManager {


    private ImageMaps plugin;
    private List<Short> sendList;
    private Map<Short, ImageMap> maps;
    private Map<String, BufferedImage> images = new HashMap<>(); // Declare the 'images' field here



    public MapManager(ImageMaps plugin) {
        this.plugin = plugin;
        this.sendList = plugin.getFastSendList();
    }


    private BufferedImage loadImage(String file) {
        if (images.containsKey(file))
            return images.get(file);

        File f = new File(plugin.getDataFolder(), "images" + File.separatorChar + file);
        BufferedImage image = null;

        if (!f.exists())
            return null;

        try {
            image = ImageIO.read(f);
            images.put(file, image);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }




    private void loadMaps() {
        File file = new File(plugin.getDataFolder(), "maps.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (String key : config.getKeys(false))
        {
            short id = Short.parseShort(key);
            MapView map = plugin.getServer().getMap(id);

            for (MapRenderer r : map.getRenderers())
                map.removeRenderer(r);

            String image = config.getString(key + ".image");
            int x = config.getInt(key + ".x");
            int y = config.getInt(key + ".y");
            boolean fastsend = config.getBoolean(key + ".fastsend", false);

            BufferedImage bimage = loadImage(image);

            if (bimage == null)
            {
                getLogger().warning("Image file " + image + " not found, removing this map!");
                continue;
            }

            if (fastsend)
                sendList.add(id);

            map.addRenderer(new ImageMapRenderer(loadImage(image), x, y));
            maps.put(id, new ImageMap(image, x, y, fastsend));
        }
    }



    // SAVE MAP

    private void saveMaps() {
        File file = new File(plugin.getDataFolder(), "maps.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (String key : config.getKeys(false))
            config.set(key, null);

        for (Map.Entry<Short, ImageMap> e : maps.entrySet())
        {
            config.set(e.getKey() + ".image", e.getValue().getImage());
            config.set(e.getKey() + ".x", e.getValue().getX());
            config.set(e.getKey() + ".y", e.getValue().getY());
            config.set(e.getKey() + ".fastsend", e.getValue().isFastSend());
        }

        try
        {
            config.save(file);
        }
        catch (IOException e1)
        {
            getLogger().severe("Failed to save maps.yml!");
            e1.printStackTrace();
        }
    }

}

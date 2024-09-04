package de.craftlancer.imagemaps;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import de.craftlancer.imagemaps.image.ImageMap;
import de.craftlancer.imagemaps.cmd.ImageMapCommand;
import de.craftlancer.imagemaps.image.ImageMapRenderer;
import de.craftlancer.imagemaps.other.FastSendTask;
import de.craftlancer.imagemaps.other.PlacingCacheEntry;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.Metrics;

public class ImageMaps extends JavaPlugin
{
    public static final int MAP_WIDTH = 128;
    public static final int MAP_HEIGHT = 128;
    
    private Map<String, PlacingCacheEntry> placing = new HashMap<String, PlacingCacheEntry>();
    private Map<Short, ImageMap> maps = new HashMap<Short, ImageMap>();
    private Map<String, BufferedImage> images = new HashMap<String, BufferedImage>();
    private List<Short> sendList = new ArrayList<Short>();
    private FastSendTask sendTask;
    
    @Override
    public void onEnable()
    {
        if (!new File(getDataFolder(), "images").exists())
            new File(getDataFolder(), "images").mkdirs();
        
        int sendPerTicks = getConfig().getInt("sendPerTicks", 20);
        int mapsPerSend = getConfig().getInt("mapsPerSend", 8);

        // Register the command

        getCommand("imagemap").setExecutor(new ImageMapCommand(this));

        // Register the listener

        sendTask = new FastSendTask(this, mapsPerSend);



        getServer().getPluginManager().registerEvents(sendTask, this);
        sendTask.runTaskTimer(this, sendPerTicks, sendPerTicks);

        try {
            Metrics metrics = new Metrics(this); // 'this' refers to the instance of ImageMaps which is a subclass of JavaPlugin
            metrics.start();
        } catch (IOException e) {
            getLogger().severe("Failed to load Metrics!");
        }
    }
    
    @Override
    public void onDisable()
    {
        getServer().getScheduler().cancelTasks(this);
    }
    
    public List<Short> getFastSendList()
    {
        return sendList;
    }
    
    public void startPlacing(Player p, String image, boolean fastsend)
    {
        placing.put(p.getName(), new PlacingCacheEntry(image, fastsend));
    }








    private BufferedImage loadImage(String file)
    {
        if (images.containsKey(file))
            return images.get(file);

        File f = new File(getDataFolder(), "images" + File.separatorChar + file);
        BufferedImage image = null;

        if (!f.exists())
            return null;

        try
        {
            image = ImageIO.read(f);
            images.put(file, image);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return image;
    }





    public void reloadImage(String file)
    {
        images.remove(file);
        BufferedImage image = loadImage(file);

        int width = (int) Math.ceil((double) image.getWidth() / (double) MAP_WIDTH);
        int height = (int) Math.ceil((double) image.getHeight() / (double) MAP_HEIGHT);

        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
            {
                short id = getMapItem(file, x * MAP_WIDTH, y * MAP_HEIGHT, image).getDurability();
                MapView map = getServer().getMap(id);

                for (MapRenderer renderer : map.getRenderers())
                    if (renderer instanceof ImageMapRenderer)
                        ((ImageMapRenderer) renderer).recalculateInput(image, x * MAP_WIDTH, y * MAP_HEIGHT);

                sendTask.addToQueue(id);
            }

    }
}

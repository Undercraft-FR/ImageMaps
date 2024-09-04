package de.craftlancer.imagemaps.image;

// ImagePlacer.java
import de.craftlancer.imagemaps.ImageMaps;
import de.craftlancer.imagemaps.other.PlacingCacheEntry;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static de.craftlancer.imagemaps.ImageMaps.MAP_HEIGHT;
import static de.craftlancer.imagemaps.ImageMaps.MAP_WIDTH;
import static org.bukkit.Bukkit.getLogger;

public class ImagePlacer {
    private ImageMaps plugin;

    public ImagePlacer(ImageMaps plugin) {
        this.plugin = plugin;
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



    public boolean placeImage(Block block, BlockFace face, PlacingCacheEntry cache)
    {
        int xMod = 0;
        int zMod = 0;

        switch (face)
        {
            case EAST:
                zMod = -1;
                break;
            case WEST:
                zMod = 1;
                break;
            case SOUTH:
                xMod = 1;
                break;
            case NORTH:
                xMod = -1;
                break;
            default:
                getLogger().severe("Someone tried to create an image with an invalid block facing");
                return false;
        }

        BufferedImage image = loadImage(cache.getImage());

        if (image == null)
        {
            getLogger().severe("Someone tried to create an image with an invalid file!");
            return false;
        }

        Block b = block.getRelative(face);

        int width = (int) Math.ceil((double) image.getWidth() / (double) MAP_WIDTH);
        int height = (int) Math.ceil((double) image.getHeight() / (double) MAP_HEIGHT);

        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                if (!block.getRelative(x * xMod, -y, x * zMod).getType().isSolid())
                    return false;

        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                setItemFrame(b.getRelative(x * xMod, -y, x * zMod), image, face, x * MAP_WIDTH, y * MAP_HEIGHT, cache);

        return true;
    }

    private void setItemFrame(Block bb, BufferedImage image, BlockFace face, int x, int y, PlacingCacheEntry cache)
    {
        bb.setType(Material.AIR);
        ItemFrame i = bb.getWorld().spawn(bb.getRelative(face.getOppositeFace()).getLocation(), ItemFrame.class);
        i.teleport(bb.getLocation());
        i.setFacingDirection(face, true);

        ItemStack item = getMapItem(cache.getImage(), x, y, image);
        i.setItem(item);

        short id = item.getDurability();

        if (cache.isFastSend() && !sendList.contains(id))
        {
            sendList.add(id);
            sendTask.addToQueue(id);
        }

        maps.put(id, new ImageMap(cache.getImage(), x, y, sendList.contains(id)));
    }

    private ItemStack getMapItem(String file, int x, int y, BufferedImage image) {
        ItemStack item = new ItemStack(Material.MAP, 1, (short) 0);
        MapView view = plugin.getServer().createMap(plugin.getServer().getWorlds().get(0));
        MapRenderer renderer = new ImageMapRenderer(image, x, y);
        view.addRenderer(renderer);
        item.setDurability(view.getId());
        return item;
    }
}
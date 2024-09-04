package de.craftlancer.imagemaps.image;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ImageInteract implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent e)
    {
        if (!e.hasBlock())
            return;

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (!placing.containsKey(e.getPlayer().getName()))
            return;

        if (!placeImage(e.getClickedBlock(), e.getBlockFace(), placing.get(e.getPlayer().getName())))
            e.getPlayer().sendMessage("Can't place the image here!");

        placing.remove(e.getPlayer().getName());

    }
}

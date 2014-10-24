package com.jcwhatever.bukkit.rental.events;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jcwhatever.bukkit.generic.economy.EconomyHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.jcwhatever.bukkit.rental.BillCollector;
import com.jcwhatever.bukkit.rental.Msg;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;
import com.jcwhatever.bukkit.rental.region.RentRegion;

public class GlobalListener implements Listener {

    private RentRegionManager _manager;

    public GlobalListener(RentRegionManager manager) {
        _manager = manager;
    }

    @EventHandler(priority=EventPriority.NORMAL)
    private void onBlockPlace(BlockPlaceEvent event) {
        Location location = event.getBlockPlaced().getLocation();

        RentRegion region = _manager.getRegion(location);
        if (region == null)
            return;

        if (!region.canInteract(event.getPlayer(), location)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.NORMAL)
    private void onBlockDamage(BlockDamageEvent event) {

        if (event.getPlayer() == null)
            return;

        Location location = event.getBlock().getLocation();

        RentRegion region = _manager.getRegion(location);
        if (region == null)
            return;

        if (!region.canInteract(event.getPlayer(), location)) {
            event.setCancelled(true);
        }

    }

    @EventHandler(priority=EventPriority.NORMAL)
    private void onBlockBreak(BlockBreakEvent event) {

        if (event.getPlayer() == null)
            return;

        Location location = event.getBlock().getLocation();

        RentRegion region = _manager.getRegion(location);
        if (region == null)
            return;

        if (!region.canInteract(event.getPlayer(), location)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.NORMAL)
    private void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getPlayer() == null)
            return;

        Location location = event.getBlock().getLocation();

        RentRegion region = _manager.getRegion(location);
        if (region == null)
            return;

        if (!region.canInteract(event.getPlayer(), location)) {
            event.setCancelled(true);
        }	
    }



    @EventHandler(priority=EventPriority.NORMAL)
    private void onPlayerInteract(PlayerInteractEvent event) {

        if (!event.hasBlock())
            return;

        Location location = event.getClickedBlock().getLocation();

        // Allow opening and closing doors and chests in non-rent regions
        Block block = event.getClickedBlock();
        boolean isDeviceInteraction = 
                (event.getAction() == Action.RIGHT_CLICK_BLOCK ||
                event.getAction() == Action.PHYSICAL) && 

                (block.getType() == Material.WOODEN_DOOR ||
                block.getType() == Material.IRON_DOOR ||
                block.getType() == Material.TRAP_DOOR ||
                block.getType() == Material.FENCE_GATE ||
                block.getType() == Material.CHEST ||
                block.getType() == Material.LEVER ||
                block.getType() == Material.WOOD_BUTTON ||
                block.getType() == Material.STONE_BUTTON ||
                block.getType() == Material.WOOD_PLATE ||
                block.getType() == Material.STONE_PLATE ||
                block.getType() == Material.GOLD_PLATE);

        RentRegion region = _manager.getRegion(location);
        if (region == null) {
            
            if (isDeviceInteraction)
                return;

            if (_manager.isProtectedWorld(location.getWorld())) {
                event.setCancelled(true);
                event.setUseInteractedBlock(Result.DENY);
                //event.setUseItemInHand(Result.DENY);
            }
            return;
        }
        
        if (!region.hasTenant() && isDeviceInteraction)
            return;
            
        if (!region.isTenantOrFriend(event.getPlayer()) && !region.isEditModeOn()) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Result.DENY);
            //event.setUseItemInHand(Result.DENY);
        }
    }

    @EventHandler
    private void onPriceChanged(RentPriceChangedEvent event) {

        RentRegionManager manager = RentalRooms.getInstance().getRegionManager();
        BillCollector billCollector = RentalRooms.getInstance().getBillCollector();

        List<RentRegion> regions = manager.getRegions();
        Set<Player> notified = new HashSet<Player>();

        for (RentRegion region : regions) {
            if (!region.hasTenant())
                continue;

            Player p = region.getTenant().getPlayer();

            if (p != null) {

                if (notified.contains(p))
                    continue;

                Msg.tell(p, "Rent has increased from {0} to {1} per cubic volume.",  EconomyHelper.formatAmount(event.getOldAmount()), EconomyHelper.formatAmount(event.getNewAmount()));
                notified.add(p);
            }
        }

    }

}

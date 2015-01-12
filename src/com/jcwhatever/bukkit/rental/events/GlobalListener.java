/* This file is part of RentalRooms for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package com.jcwhatever.bukkit.rental.events;

import com.jcwhatever.nucleus.utils.Economy;
import com.jcwhatever.bukkit.rental.BillCollector;
import com.jcwhatever.bukkit.rental.Msg;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.region.RentRegion;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

                Msg.tell(p, "Rent has increased from {0} to {1} per cubic volume.",
                        Economy.getCurrency().format(event.getOldAmount()),
                        Economy.getCurrency().format(event.getNewAmount()));
                notified.add(p);
            }
        }

    }

}

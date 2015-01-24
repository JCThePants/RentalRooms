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


package com.jcwhatever.rentalrooms.events;

import com.jcwhatever.rentalrooms.Lang;
import com.jcwhatever.rentalrooms.Msg;
import com.jcwhatever.rentalrooms.RentalRooms;
import com.jcwhatever.rentalrooms.region.RentRegion;
import com.jcwhatever.rentalrooms.region.RentRegionManager;
import com.jcwhatever.nucleus.utils.extended.MaterialExt;
import com.jcwhatever.nucleus.utils.language.Localizable;

import org.bukkit.Location;
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GlobalListener implements Listener {

    @Localizable static final String _RENT_INCREASED =
            "Rent has increased from {0: previous amount} to {1: new amount} per cubic volume.";

    @Localizable static final String _RENT_DECREASED =
            "Rent has decreased from {0: previous amount} to {1: new amount} per cubic volume.";

    private RentRegionManager _manager;

    public GlobalListener(RentRegionManager manager) {
        _manager = manager;
    }

    /**
     * Prevent strangers from placing blocks inside a rent region.
     */
    @EventHandler(priority=EventPriority.NORMAL)
    private void onBlockPlace(BlockPlaceEvent event) {
        Location location = event.getBlockPlaced().getLocation();

        RentRegion region = _manager.get(location);
        if (region == null)
            return;

        if (!region.canInteract(event.getPlayer(), location)) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevent strangers from damaging blocks inside a rent region.
     */
    @EventHandler(priority=EventPriority.NORMAL)
    private void onBlockDamage(BlockDamageEvent event) {

        if (event.getPlayer() == null)
            return;

        Location location = event.getBlock().getLocation();

        RentRegion region = _manager.get(location);
        if (region == null)
            return;

        if (!region.canInteract(event.getPlayer(), location)) {
            event.setCancelled(true);
        }

    }

    /**
     * Prevent strangers from breaking blocks inside a rent region.
     */
    @EventHandler(priority=EventPriority.NORMAL)
    private void onBlockBreak(BlockBreakEvent event) {

        if (event.getPlayer() == null)
            return;

        Location location = event.getBlock().getLocation();

        RentRegion region = _manager.get(location);
        if (region == null)
            return;

        if (!region.canInteract(event.getPlayer(), location)) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevent strangers from igniting blocks inside a rent region.
     */
    @EventHandler(priority=EventPriority.NORMAL)
    private void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getPlayer() == null)
            return;

        Location location = event.getBlock().getLocation();

        RentRegion region = _manager.get(location);
        if (region == null)
            return;

        if (!region.canInteract(event.getPlayer(), location)) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevent strangers from performing certain interactions inside a rent region.
     */
    @EventHandler(priority=EventPriority.NORMAL)
    private void onPlayerInteract(PlayerInteractEvent event) {

        if (!event.hasBlock())
            return;

        Location location = event.getClickedBlock().getLocation();

        // Goal: Allow opening and closing doors and chests in non-rented regions

        Block block = event.getClickedBlock();
        MaterialExt materialExt = MaterialExt.from(block.getType());
        boolean isDeviceInteraction =
                (event.getAction() == Action.RIGHT_CLICK_BLOCK ||
                        event.getAction() == Action.PHYSICAL) &&

                        (materialExt.isOpenableBoundary() ||
                                materialExt.hasGUI() ||
                                materialExt.isRedstoneCompatible());

        RentRegion region = _manager.get(location);
        if (region == null) {

            // prevent interaction in protected world unless it is a device interaction.
            if (!isDeviceInteraction && _manager.isProtectedWorld(location.getWorld())) {
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
        }
    }

    /**
     * Notify rent owners of price changes.
     */
    @EventHandler
    private void onPriceChanged(RentPriceChangedEvent event) {

        double oldAmount = event.getOldPrice();
        double newAmount = event.getNewPrice();

        if (Double.compare(oldAmount, newAmount) == 0)
            return;

        RentRegionManager manager = RentalRooms.getRegionManager();

        Collection<RentRegion> regions = manager.getAll();
        Set<Player> notified = new HashSet<Player>(regions.size());

        String message = oldAmount > newAmount
                ? Lang.get(_RENT_DECREASED, oldAmount, newAmount)
                : Lang.get(_RENT_INCREASED, oldAmount, newAmount);

        for (RentRegion region : regions) {
            if (!region.hasTenant())
                continue;

            Player p = region.getTenant().getPlayer();

            if (p != null) {

                if (notified.contains(p))
                    continue;

                Msg.tell(p, message);
                notified.add(p);
            }
        }
    }
}

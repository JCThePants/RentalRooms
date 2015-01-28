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


package com.jcwhatever.rentalrooms;

import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.rentalrooms.region.RentRegion;
import com.jcwhatever.nucleus.utils.player.PlayerUtils;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * A rent regions player tenant. Includes static utility methods.
 */
public class Tenant {

    private static Map<UUID, Tenant> _tenants = new HashMap<UUID, Tenant>(25);

    /**
     * Get the {@code Tenant} instance for the specified player.
     *
     * @param player  The player to get a tenant instance for.
     *
     * @return  Null if the player is not a tenant of any rent regions.
     */
    @Nullable
    public static Tenant get(Player player) {
        PreCon.notNull(player);

        return get(player.getUniqueId());
    }

    /**
     * Get the {@code Tenant} instance for the specified player.
     *
     * @param playerId  The ID of the player to get a tenant instance for.
     *
     * @return  Null if the player is not a tenant of any rent regions.
     */
    public static Tenant get(UUID playerId) {
        PreCon.notNull(playerId);

        return _tenants.get(playerId);
    }

    /**
     * Register a player as a tenant of the specified rent region
     *
     * @param player  The player to register.
     * @param region  The rent region the player is a tenant of.
     *
     * @return  The players {@code Tenant} instance.
     */
    public static Tenant register(Player player, RentRegion region) {
        PreCon.notNull(player);

        return register(player.getUniqueId(), region);
    }

    /**
     * Register a player as a tenant of the specified rent region
     *
     * @param playerId  The ID of the player to register.
     * @param region    The rent region the player is a tenant of.
     *
     * @return  The players {@code Tenant} instance.
     */
    public static Tenant register(UUID playerId, RentRegion region) {
        PreCon.notNull(playerId);
        PreCon.notNull(region);

        Tenant tenant = get(playerId);
        if (tenant == null) {
            tenant = new Tenant(playerId);
            _tenants.put(playerId, tenant);
        }

        tenant._rentRegions.add(region);

        return tenant;
    }

    /**
     * Evict a player from all rent regions.
     *
     * @param player  The player to evict.
     */
    public static void evictAll(Player player) {
        evictAll(player.getUniqueId());
    }

    /**
     * Evict a player from all rent regions.
     *
     * @param playerId  The ID of the player to evict.
     */
    public static void evictAll(UUID playerId) {
        Tenant tenant = get(playerId);
        if (tenant == null)
            return;

        for (RentRegion region : tenant._rentRegions) {
            region.evict();
        }

        _tenants.remove(playerId);
    }


    private final UUID _playerId;
    private final Set<RentRegion> _rentRegions = new HashSet<RentRegion>(5);

    /**
     * Constructor.
     *
     * @param playerId    The ID of the tenant player.
     */
    private Tenant(UUID playerId) {
        _playerId = playerId;
    }

    /**
     * Get the player ID of the tenant.
     */
    public UUID getPlayerID() {
        return _playerId;
    }

    /**
     * Get the {@code Player} object of the tenant if available.
     *
     * @return  Null if the player is not online.
     */
    @Nullable
    public Player getPlayer() {
        return PlayerUtils.getPlayer(getPlayerID());
    }

    /**
     * Get the tenants player name.
     */
    public String getPlayerName() {
        String playerName = PlayerUtils.getPlayerName(_playerId);
        return playerName == null
                ? "?"
                : playerName;
    }

    /**
     * Get all rent regions the tenant is renting.
     */
    public Collection<RentRegion> getRentRegions() {
        return Collections.unmodifiableCollection(_rentRegions);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UUID) {
            return obj.equals(_playerId);
        }
        else if (obj instanceof Player) {
            return ((Player)obj).getUniqueId().equals(_playerId);
        }
        else if (obj instanceof Tenant) {
            return ((Tenant)obj)._playerId.equals(_playerId);
        }
        return false;
    }
}

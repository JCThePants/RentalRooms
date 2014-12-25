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


package com.jcwhatever.bukkit.rental;

import com.jcwhatever.generic.utils.player.PlayerUtils;
import com.jcwhatever.bukkit.rental.region.RentRegion;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

public class Tenant {

	private static Map<UUID, Tenant> _tenants = new HashMap<UUID, Tenant>();

	private UUID _playerId;
	private UUID _landlordId;
	private Set<RentRegion> _rentRegions = new HashSet<RentRegion>();
	
	public static Tenant get(Player p) {
		return get(p.getUniqueId());
	}

	public static Tenant get(UUID playerId) {
		return _tenants.get(playerId);
	}

	public static Tenant add(Player p, RentRegion region) {
		return add(p.getUniqueId(), region);
	}

	public static Tenant add(UUID playerId, RentRegion region) {
		Tenant tenant = get(playerId);
		if (tenant == null) {
			tenant = new Tenant(playerId);
			_tenants.put(playerId, tenant);	
		}
		
		tenant._rentRegions.add(region);
		
		return tenant;
	}

	public static void evictAll(Player p) {
		evictAll(p.getUniqueId());
	}

	public static void evictAll(UUID playerId) {
		Tenant tenant = get(playerId);
		if (tenant == null)
			return;

		for (RentRegion region : tenant._rentRegions) {
			region.evict();
		}

		_tenants.remove(playerId);
	}
	
	


	private Tenant(UUID playerId, UUID landlordId) {
		_playerId = playerId;
		_landlordId = landlordId;
	}


	private Tenant(UUID playerId) {
		_playerId = playerId;
	}

	public UUID getPlayerID() {
		return _playerId;
	}
	
	public String getPlayerName() {
		String playerName = PlayerUtils.getPlayerName(_playerId);
        return playerName == null
                ? "?"
                : playerName;
	}

	public UUID getLandlordPlayerID() {
		return _landlordId;
	}
	
	public List<RentRegion> getRentRegions() {
		return new ArrayList<RentRegion>(_rentRegions);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UUID) {
			return ((UUID)obj).equals(_playerId);
		}
		else if (obj instanceof Player) {
			return ((Player)obj).getUniqueId().equals(_playerId);
		}
		else if (obj instanceof Tenant) {
			return ((Tenant)obj)._playerId.equals(_playerId);
		}
		return false;
	}

    @Nullable
	public Player getPlayer() {
		return PlayerUtils.getPlayer(getPlayerID());
	}

}

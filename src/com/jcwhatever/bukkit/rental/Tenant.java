package com.jcwhatever.bukkit.rental;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.jcwhatever.bukkit.generic.player.PlayerHelper;
import com.jcwhatever.bukkit.rental.region.RentRegion;

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
		return PlayerHelper.getPlayerName(_playerId);		
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

	public Player getPlayer() {
		return PlayerHelper.getPlayer(getPlayerID());
	}

}

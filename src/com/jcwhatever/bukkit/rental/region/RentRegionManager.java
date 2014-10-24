package com.jcwhatever.bukkit.rental.region;

import com.jcwhatever.bukkit.generic.GenericsLib;
import com.jcwhatever.bukkit.generic.performance.SingleCache;
import com.jcwhatever.bukkit.generic.regions.ReadOnlyRegion;
import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.rental.Tenant;
import com.sun.istack.internal.Nullable;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RentRegionManager {

	private IDataNode _settings;
	private Map<String, RentRegion> _namedRegions;
	private List<RentRegion> _regions;
	private Map<Tenant, RentRegion> _tenantRegions;
	private Set<String> _protectedWorlds = new HashSet<String>();
	
	
	private SingleCache<Location, RentRegion> _lastRegionByLocation = new SingleCache<Location, RentRegion>();
	
	public RentRegionManager(IDataNode settings) {
		
		_settings = settings;
		_namedRegions = new HashMap<String, RentRegion>();
		_regions = new ArrayList<RentRegion>();
		_tenantRegions = new HashMap<Tenant, RentRegion>();
				
		loadRegions();
		loadSettings();
	}
	
	public boolean isProtectedWorld(World world) {
		return _protectedWorlds.contains(world.getName());
	}
	
	public boolean removeProtectedWorld(World world) {
		return removeProtectedWorld(world.getName());
	}
	
	public boolean removeProtectedWorld(String worldName) {
		if (_protectedWorlds.remove(worldName)) {
			_settings.set("protected-worlds", new ArrayList<String>(_protectedWorlds));
			_settings.saveAsync(null);
			return true;
		}
		return false;
	}
	
	public boolean addProtectedWorld(World world) {
		if (_protectedWorlds.add(world.getName())) {
			_settings.set("protected-worlds", new ArrayList<String>(_protectedWorlds));
			_settings.saveAsync(null);
			return true;
		}
		return false;
	}
	
	
	public List<String> getProtectedWorlds() {
		return new ArrayList<String>(_protectedWorlds);
	}
	
	
	public RentRegion getRegion(String regionName) {
		regionName = regionName.toLowerCase();
		return _namedRegions.get(regionName);
	}		

    @Nullable
	public RentRegion getRegion(Location location) {
		
		if (_lastRegionByLocation.keyEquals(location))
			return _lastRegionByLocation.getValue();
		
		Set<ReadOnlyRegion> regions = GenericsLib.getRegionManager().getRegions(location);
		
		for (ReadOnlyRegion readOnlyRegion : regions) {
			if (RentRegion.class.isAssignableFrom(readOnlyRegion.getHandleClass())) {

                RentRegion region = getRegion(readOnlyRegion.getName());
				_lastRegionByLocation.set(location, region);

				return region;
			}
		}
		
		_lastRegionByLocation.set(location, null);
		return null;
	}
	
	public List<RentRegion> getRegions() {
		return new ArrayList<RentRegion>(_regions);
	}
	
	public List<RentRegion> getRegions(World world) {
		
		List<RentRegion> results = new ArrayList<RentRegion>();
		
		for (RentRegion region : _regions) {
			if (region.getWorld().equals(world))
				results.add(region);
		}
		return results;
	}
	
	public RentRegion newRegion(String regionName, final Location p1, final Location p2) {
		final RentRegion region = new RentRegion(regionName, _settings.getNode(regionName));
		
		region.setCoords(p1, p2);
					
		addRegionToCollections(region);
		
		return region;
	}
	
	public boolean removeRegion(String regionName) {
	    regionName = regionName.toLowerCase();
	    
	    RentRegion region = _namedRegions.get(regionName);
	    if (region == null)
	        return false;
	    
	    IDataNode node =  _settings.getNode(regionName);;
	    node.remove();
	    node.saveAsync(null);
	    
	    region.dispose();
	    
	    removeRegionFromCollections(region);
	    
	    return true;
	}
	
	private void loadRegions() {
		Set<String> regionNames = _settings.getSubNodeNames();
		
		if (regionNames == null)
			return;
		
		for (String regionName : regionNames) {
			IDataNode regionSettings = _settings.getNode(regionName);
			
			RentRegion region = new RentRegion(regionName, regionSettings);
			
			if (region.hasTenant()) {
				_tenantRegions.put(region.getTenant(), region);
			}
			
			addRegionToCollections(region);
		}
	}
	
	private void loadSettings() {
		
		List<String> worldNames = _settings.getStringList("protected-worlds", null);
		if (worldNames == null) {
			_protectedWorlds.clear();
		}
		else {
			_protectedWorlds.addAll(worldNames);
		}
	}

	
	
	private void addRegionToCollections(RentRegion region) {
		_namedRegions.put(region.getSearchName(), region);
		
		_regions.add(region);
		
		if (region.hasTenant()) {
			_tenantRegions.put(region.getTenant(), region);
		}
	}
	
	private void removeRegionFromCollections(RentRegion region) {
        _namedRegions.remove(region.getSearchName());
        
        _regions.remove(region);
        
        if (region.hasTenant()) {
            _tenantRegions.remove(region.getTenant());
        }
    }
	
}

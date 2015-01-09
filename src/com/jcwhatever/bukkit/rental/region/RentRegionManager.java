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


package com.jcwhatever.bukkit.rental.region;

import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.utils.performance.SingleCache;
import com.jcwhatever.nucleus.regions.IRegion;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.bukkit.rental.Tenant;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

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
		
		List<IRegion> regions = Nucleus.getRegionManager().getRegions(location);
		
		for (IRegion readOnlyRegion : regions) {
			if (RentRegion.class.isAssignableFrom(readOnlyRegion.getRegionClass())) {

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

		for (IDataNode regionNode : _settings) {

			RentRegion region = new RentRegion(regionNode.getName(), regionNode);
			
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

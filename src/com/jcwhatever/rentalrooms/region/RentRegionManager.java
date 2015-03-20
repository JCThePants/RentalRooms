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


package com.jcwhatever.rentalrooms.region;

import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.regions.selection.IRegionSelection;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.CollectionUtils;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.managers.NamedInsensitiveDataManager;
import com.jcwhatever.nucleus.utils.performance.EntryCache;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

public class RentRegionManager extends NamedInsensitiveDataManager<RentRegion> {

    private final Set<String> _protectedWorlds = new HashSet<String>(10);
    private final EntryCache<Location, RentRegion> _lastRegionByLocation = new EntryCache<Location, RentRegion>();

    public RentRegionManager(IDataNode dataNode) {
        super(dataNode.getNode("regions"), true);

        List<String> worldNames = dataNode.getStringList("protected-worlds", null);
        if (worldNames == null) {
            _protectedWorlds.clear();
        }
        else {
            _protectedWorlds.addAll(worldNames);
        }
    }

    public boolean isProtectedWorld(World world) {
        return _protectedWorlds.contains(world.getName());
    }

    public boolean removeProtectedWorld(World world) {
        PreCon.notNull(world);

        return removeProtectedWorld(world.getName());
    }

    public boolean removeProtectedWorld(String worldName) {
        PreCon.notNull(worldName);

        if (_protectedWorlds.remove(worldName)) {

            assert _dataNode != null;
            _dataNode.set("protected-worlds", new ArrayList<String>(_protectedWorlds));
            _dataNode.save();

            return true;
        }
        return false;
    }

    public boolean addProtectedWorld(World world) {
        if (_protectedWorlds.add(world.getName())) {
            assert _dataNode != null;
            _dataNode.set("protected-worlds", new ArrayList<String>(_protectedWorlds));
            _dataNode.save();
            return true;
        }
        return false;
    }

    public List<String> getProtectedWorlds() {
        return CollectionUtils.unmodifiableList(_protectedWorlds);
    }

    @Nullable
    public RentRegion get(Location location) {

        if (_lastRegionByLocation.keyEquals(location))
            return _lastRegionByLocation.getValue();

        List<RentRegion> regions = Nucleus.getRegionManager().getRegions(location, RentRegion.class);

        if (regions.isEmpty()) {
            _lastRegionByLocation.set(location, null);
            return null;
        }

        RentRegion region = regions.get(0);
        _lastRegionByLocation.set(location, region);
        return region;

    }

    public List<RentRegion> get(World world) {

        Collection<RentRegion> all = getAll();
        List<RentRegion> results = new ArrayList<RentRegion>(all.size());

        for (RentRegion region : all) {
            if (region.getWorld() == null)
                continue;

            if (region.getWorld().equals(world))
                results.add(region);
        }
        return results;
    }

    public RentRegion add(String regionName, IRegionSelection selection) {
        PreCon.notNullOrEmpty(regionName);
        PreCon.notNull(selection);

        assert _dataNode != null;
        RentRegion region = new RentRegion(regionName, _dataNode.getNode(regionName));
        region.setCoords(selection.getP1(), selection.getP2());

        add(region);

        return region;
    }

    @Override
    protected void onRemove(RentRegion removed) {
        super.onRemove(removed);
        removed.dispose();
    }

    @Nullable
    @Override
    protected RentRegion load(String name, IDataNode itemNode) {
        return new RentRegion(name, itemNode);
    }

    @Override
    protected void save(RentRegion item, IDataNode itemNode) {
        // do nothing
    }
}

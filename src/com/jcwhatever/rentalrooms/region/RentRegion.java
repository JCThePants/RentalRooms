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

import com.jcwhatever.nucleus.providers.friends.FriendLevels;
import com.jcwhatever.nucleus.providers.friends.IFriend;
import com.jcwhatever.nucleus.regions.RestorableRegion;
import com.jcwhatever.nucleus.regions.file.IRegionFileFactory;
import com.jcwhatever.nucleus.regions.file.IRegionFileLoader.LoadSpeed;
import com.jcwhatever.nucleus.regions.file.basic.BasicFileFactory;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.DateUtils;
import com.jcwhatever.nucleus.providers.friends.Friends;
import com.jcwhatever.nucleus.utils.coords.LocationUtils;
import com.jcwhatever.nucleus.utils.MetaKey;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.managed.scheduler.Scheduler;
import com.jcwhatever.nucleus.utils.file.NucleusByteReader;
import com.jcwhatever.nucleus.utils.file.NucleusByteWriter;
import com.jcwhatever.nucleus.utils.astar.InteriorFinder;
import com.jcwhatever.nucleus.utils.astar.InteriorFinder.InteriorResults;
import com.jcwhatever.rentalrooms.BillCollector;
import com.jcwhatever.rentalrooms.Msg;
import com.jcwhatever.rentalrooms.RentalRooms;
import com.jcwhatever.rentalrooms.Tenant;
import com.jcwhatever.rentalrooms.events.RentMoveInEvent;
import com.jcwhatever.rentalrooms.events.RentMoveOutEvent;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A region that can be rented.
 */
public class RentRegion extends RestorableRegion {

    private static final int INTERIOR_FILE_VERSION = 1;
    private static final Location INTERACT_LOCATION = new Location(null, 0, 0, 0);
    
    public static final MetaKey<RentRegion> REGION_META_KEY = new MetaKey<RentRegion>(RentRegion.class);

    private Tenant _tenant;
    private Set<Location> _tenantArea;
    private boolean _isEditModeOn = false;
    private Date _rentExpiration = null;

    private final IDataNode _dataNode;
    private final BasicFileFactory _fileFactory = new BasicFileFactory("rent");

    /**
     * Constructor.
     *
     * @param name      The name of the rent region.
     * @param dataNode  The region data node.
     */
    public RentRegion(String name, IDataNode dataNode) {
        super(RentalRooms.getPlugin(), name, dataNode);

        PreCon.notNull(dataNode);

        _dataNode = dataNode;
        _tenantArea = new HashSet<Location>(100);

        load();
        loadInterior();

        getMeta().set(REGION_META_KEY, this);
    }

    /**
     * Flag the regions rent as payed for another cycle.
     */
    public void setPayed() {

        BillCollector billCollector = RentalRooms.getBillCollector();

        _rentExpiration = _rentExpiration == null
                ? DateUtils.addDays(new Date(), billCollector.getRentCycle())
                : DateUtils.addDays(_rentExpiration, billCollector.getRentCycle());

        SimpleDateFormat dateFormat = new SimpleDateFormat();

        _dataNode.set("rent-expiration", dateFormat.format(_rentExpiration));
        _dataNode.save();
    }

    /**
     * Get the data the rent expires.
     */
    public Date getExpirationDate() {
        return _rentExpiration;
    }

    /**
     * Get the formatted date the rent expires.
     */
    public String getFormattedExpiration() {
        return RentalRooms.formatDate(getExpirationDate());
    }

    /**
     * Determine if edit mode is on.
     *
     * <p>When edit mode is on, blocks inside the rent region can be modified.</p>
     */
    public boolean isEditModeOn() {
        return _isEditModeOn;
    }

    /**
     * Set the edit mode.
     *
     * @param isEditModeOn  True to allow editing blocks inside the region. Otherwise false.
     */
    public void setIsEditModeOn(boolean isEditModeOn) {
        _isEditModeOn = isEditModeOn;
    }

    /**
     * Set the regions tenant.
     *
     * @param player  The player that is renting the region.
     */
    public void setTenant(Player player) {
        setOwner(player.getUniqueId()); // Tenant object is set by protected method "onOwnerChanged"
    }

    /**
     * Set the regions tenant.
     *
     * @param playerId  The ID of the player that is renting the region.
     */
    public void setTenant(UUID playerId) {
        setOwner(playerId); // Tenant object is set by protected method "onOwnerChanged"
    }

    /**
     * Determine if the region has a tenant.
     */
    public boolean hasTenant() {
        return _tenant != null;
    }

    /**
     * Get the regions tenant.
     */
    public Tenant getTenant() {
        return _tenant;
    }

    /**
     * Evict the current tenant.
     */
    public void evict() {

        super.setOwner(null);

        evictionCleanup();
    }

    /**
     * Get the volume of the space that is rented.
     *
     * <p>The interior is the locations that the tenant is allowed to modify.</p>
     */
    public int getInteriorVolume() {
        return _tenantArea.size();
    }

    /**
     * Determine if a location is an interior location within the rent region.
     *
     * <p>The interior is the locations that the tenant is allowed to modify.</p>
     *
     * @param location  The location to check.
     */
    public boolean isInterior(Location location) {
        return _tenantArea.contains(location);
    }

    /**
     * Determine if the specified player is allowed to interact
     * with a location within the rent region.
     *
     * @param player    The player who is interacting.
     * @param location  The location the player is interacting with.
     */
    public boolean canInteract(Player player, Location location) {
        return canInteract(player.getUniqueId(), location);
    }

    /**
     * Determine if the specified player is allowed to interact
     * with a location within the rent region.
     *
     * @param playerId  The ID of the player who is interacting.
     * @param location  The location the player is interacting with.
     */
    public boolean canInteract(UUID playerId, Location location) {

        if (_isEditModeOn)
            return true;

        if (_tenant == null || playerId == null || location == null || !isDefined())
            return false;

        location = LocationUtils.getBlockLocation(location, INTERACT_LOCATION);

        if (!_tenantArea.contains(location))
            return false;

        if (_tenant.getPlayerID().equals(playerId))
            return true;

        IFriend friend = Friends.get(_tenant.getPlayerID(), playerId);
        return friend != null && friend.getRawLevel() < FriendLevels.CLOSE.getRawLevel();
    }

    /**
     * Determine if a player is either the regions tenant or a
     * friend of the tenant.
     *
     * @param player  The player to check.
     */
    public boolean isTenantOrFriend(Player player) {
        return isTenantOrFriend(player.getUniqueId());
    }

    /**
     * Determine if a player is either the regions tenant or a
     * friend of the tenant.
     *
     * @param playerId  The ID of the player to check.
     */
    public boolean isTenantOrFriend(UUID playerId) {
        return _tenant != null &&
                (_tenant.getPlayerID().equals(playerId) || Friends.isFriend(_tenant.getPlayerID(), playerId));
    }

    /**
     * Search for interior locations within the rent region starting from
     * the specified location.
     *
     * <p>The algorithm searches from the start location for all air blocks
     * within a confined space within the region. The rent region is the max
     * boundary the search can reach.</p>
     *
     * @param start  The start location.
     *
     * @return  The number of interior locations found.
     */
    public int addInterior(Location start) {

        InteriorFinder finder = new InteriorFinder();
        InteriorResults results = finder.searchInterior(start, this);

        Set<Location> interior = results.getInterior();
        _tenantArea.addAll(interior);

        saveInterior();

        return interior.size();
    }

    /**
     * Clear stored interior locations and delete cached interior files.
     */
    public void clearInterior() {
        _tenantArea.clear();
        try {
            getInteriorFile(true); // delete interior file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IRegionFileFactory getFileFactory() {
        return _fileFactory;
    }

    @Override
    protected boolean onOwnerChanged(UUID oldOwnerId, UUID ownerId) {

        if (_tenant != null) {
            evictionCleanup();
        }

        if (ownerId != null) {
            _tenant = Tenant.register(ownerId, this);

            RentMoveInEvent.call(this, this, _tenant);
        }

        return true;
    }

    @Override
    protected void onDispose() {
        super.onDispose();

        clearInterior();
    }

    // initial load of settings from data node.
    private void load() {
        UUID tenantId = super.getOwnerId();
        if (tenantId == null)
            return;

        _tenant = Tenant.register(tenantId, this);

        _rentExpiration = null;

        //noinspection ConstantConditions
        String expiresStr = getDataNode().getString("rent-expiration");
        if (expiresStr != null) {
            SimpleDateFormat format = new SimpleDateFormat();
            try {
                _rentExpiration = format.parse(expiresStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    // perform eviction actions without changing the region owner
    // and causing a region owner change event.
    private void evictionCleanup() {

        if (_tenant == null)
            return;

        Tenant oldTenant = _tenant;

        _tenant = null;

        if (this.canRestore()) {
            try {
                this.restoreData(LoadSpeed.PERFORMANCE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        _rentExpiration = null;

        //noinspection ConstantConditions
        getDataNode().set("rent-expiration", null);
        getDataNode().save();

        RentMoveOutEvent.call(this, this, oldTenant);
    }

    // get the regions interior cache file.
    private File getInteriorFile(boolean deleteIfExists) throws IOException {

        File interiorDir = new File(_fileFactory.getDirectory(this), "interiors");
        if (!interiorDir.exists() && !interiorDir.mkdir())
            throw new IOException("Failed to create interior file folder.");

        File file = new File(interiorDir, "rent." + getName() + ".bin");
        if (deleteIfExists && file.exists() && !file.delete())
            throw new IOException("Failed to delete interior file.");

        return file;
    }

    // load the regions tenant area from cache file.
    private void loadInterior() {

        File file;

        try {
            file = getInteriorFile(false);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (!file.exists())
            return;

        Scheduler.runTaskLaterAsync(RentalRooms.getPlugin(), 1, new LoadInterior(this, file));
    }

    // save the regions tenant area to a cache file.
    private void saveInterior() {

        File file;

        try {
            file = getInteriorFile(true);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Scheduler.runTaskLaterAsync(RentalRooms.getPlugin(), 1, new SaveInterior(this, _tenantArea, file));
    }

    /*
     * Runnable to load interior from cache file.
     */
    private static final class LoadInterior implements Runnable {

        final RentRegion region;
        final File file;
        List<Location> interior;
        NucleusByteReader reader;

        LoadInterior(RentRegion region, File file) {
            this.region = region;
            this.file = file;

            try {
                this.reader = new NucleusByteReader(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            if (reader == null)
                return;

            try {

                int fileVersion = reader.getInteger(); // get version

                if (fileVersion != INTERIOR_FILE_VERSION) {
                    Msg.warning("Failed to load interior file because it's not the correct " +
                            "version: {0}", file.getName());
                    return;
                }

                reader.getString(); // get region name

                String worldName = reader.getString(); // get world name
                World world = Bukkit.getWorld(worldName);

                if (world == null) {
                    Msg.warning("Failed to load interior file because the world " +
                            "it's in ({0}) is not loaded: {1}", worldName, file.getName());
                    return;
                }

                reader.getLocation(); // p1
                reader.getLocation(); // p2

                int size = reader.getInteger();

                interior = new ArrayList<Location>(size + 2);

                for (int i=0; i < size; i++) {
                    int x = reader.getInteger();
                    int y = reader.getInteger();
                    int z = reader.getInteger();
                    interior.add(new Location(world, x, y, z));
                }

            } catch (IOException e) {
                e.printStackTrace();

                return;

            } finally {
                try {
                    if (reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Scheduler.runTaskSync(RentalRooms.getPlugin(), new Runnable () {
                @Override
                public void run() {
                    region._tenantArea.clear();
                    region._tenantArea.addAll(interior);
                }
            });
        }
    }

    /**
     * Runnable to save interior to cache file.
     */
    private static final class SaveInterior implements Runnable {

        final RentRegion region;
        final List<Location> interior;
        final File file;

        SaveInterior(RentRegion region, Set<Location> interior, File file) {
            this.region = region;
            this.interior = new ArrayList<Location>(interior);
            this.file = file;
        }

        @Override
        public void run() {

            NucleusByteWriter writer = null;

            try {

                if (file.exists() && !file.delete())
                    throw new IOException("Failed to delete existing interior file.");

                if (!file.createNewFile())
                    throw new IOException("Failed to create new interior file.");

                synchronized (NucleusByteWriter.class) {
                    writer = new NucleusByteWriter(new FileOutputStream(file));
                }

                assert region.getWorld() != null;

                writer.write(INTERIOR_FILE_VERSION);
                writer.write(region.getName());
                writer.write(region.getWorld().getName());
                writer.write(region.getP1());
                writer.write(region.getP2());
                writer.write(interior.size());

                for (Location loc : interior) {
                    writer.write(loc.getBlockX());
                    writer.write(loc.getBlockY());
                    writer.write(loc.getBlockZ());
                }

                writer.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    if (writer != null)
                        writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

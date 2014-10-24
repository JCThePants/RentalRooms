package com.jcwhatever.bukkit.rental.region;

import com.jcwhatever.bukkit.generic.file.GenericsByteReader;
import com.jcwhatever.bukkit.generic.file.GenericsByteWriter;
import com.jcwhatever.bukkit.generic.pathing.InteriorFinder;
import com.jcwhatever.bukkit.generic.pathing.InteriorFinder.InteriorResults;
import com.jcwhatever.bukkit.generic.regions.BuildMethod;
import com.jcwhatever.bukkit.generic.regions.RestorableRegion;
import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.utils.DateUtils;
import com.jcwhatever.bukkit.generic.utils.LocationUtils;
import com.jcwhatever.bukkit.rental.BillCollector;
import com.jcwhatever.bukkit.rental.Msg;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.Tenant;
import com.jcwhatever.bukkit.rental.events.RentMoveInEvent;
import com.jcwhatever.bukkit.rental.events.RentMoveOutEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
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

public class RentRegion extends RestorableRegion {

	private Tenant _tenant;
	private Set<Location> _tenantArea;
	private boolean _isEditModeOn = false;
	private Date _rentExpiration = null;
	private FriendManager _friendManager;
	
	private static final int INTERIOR_FILE_VERSION = 1;
	
	IDataNode _tenantInfo;
	
	public RentRegion(String name, IDataNode settings) {
		super(RentalRooms.getInstance(), name, settings);
		_tenantArea = new HashSet<Location>();
		_tenantInfo = settings.getNode("tenant");
		loadSettings();
		loadInterior();
		
		_friendManager = new FriendManager(this, settings.getNode("friends"));
	}
	
	public FriendManager getFriendManager() {
		return _friendManager;
	}
	
	public void setPayed() {
		
		BillCollector billCollector = RentalRooms.getInstance().getBillCollector();

        _rentExpiration = _rentExpiration == null
                ? org.apache.commons.lang.time.DateUtils.addDays(new Date(), billCollector.getRentCycle())
                : org.apache.commons.lang.time.DateUtils.addDays(_rentExpiration, billCollector.getRentCycle());
		
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		
		_dataNode.set("rent-expiration", dateFormat.format(_rentExpiration));
		_dataNode.saveAsync(null);
	}
	
	public Date getExpirationDate() {
		return _rentExpiration;
	}
	
	public String getFormattedExpiration() {
		return DateUtils.format(getExpirationDate(), RentalRooms.DATE_FORMAT);
	}
	
	public boolean isEditModeOn() {
		return _isEditModeOn;
	}
	
	public void setIsEditModeOn(boolean isEditModeOn) {
		_isEditModeOn = isEditModeOn;
	}

	@Override
	protected String getFilePrefix() {
		return "rent." + getName();
	}
	
    @Override
    public boolean setOwner(UUID ownerId) {
        
        if (_tenant != null) {
            evict();
        }
        
        if (!super.setOwner(ownerId))
            return false;
        
        _tenant = Tenant.add(ownerId, this);
        
        RentMoveInEvent.callEvent(this, _tenant);
        
        return true;
    }
	
	public void setTenant(Player p) {
		setOwner(p.getUniqueId());
	}
	
	public void setTenant(UUID playerId) {
		setOwner(playerId);
	}
	
	public boolean hasTenant() {
		return _tenant != null;
	}
	
	public Tenant getTenant() {
		return _tenant;
	}
	
	public void evict() {
		
	    super.setOwner(null);
	    
		Tenant oldTenant = _tenant;
		
		_tenant = null;
		
		if (this.canRestore()) {
            try {
                this.restoreData(BuildMethod.PERFORMANCE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        _rentExpiration = null;
		_dataNode.set("rent-expiration", null);
        _dataNode.saveAsync(null);
		
		RentMoveOutEvent.callEvent(this, oldTenant);
	}
		
	public int getRentSpaceVolume() {
		return _tenantArea.size();
	}
	
	public boolean isTenantArea(Location location) {
		return _tenantArea.contains(location);
	}
	
	public boolean canInteract(Player p, Location location) {
		return canInteract(p.getUniqueId(), location);
	}
	
	public boolean canInteract(UUID playerId, Location location) {
		
		if (_isEditModeOn)
			return true;
		
		location = LocationUtils.getBlockLocation(location);
		
		if (_tenant == null || playerId == null || location == null || !isDefined())
			return false;

        // tenant equals method works with UUID's
        return (_tenant.equals(playerId) || _friendManager.hasFriend(playerId))
                && _tenantArea.contains(location);

    }
	
	public boolean isTenantOrFriend(Player p) {
		return isTenantOrFriend(p.getUniqueId());		
	}
	
	public boolean isTenantOrFriend(UUID playerId) {
        return _tenant != null &&
                (_tenant.equals(playerId) || _friendManager.hasFriend(playerId));

    }
		
	public int addInterior(Location start) {

		InteriorFinder finder = new InteriorFinder();
		InteriorResults results = finder.searchInterior(start, this);
		
		Set<Location> interior = results.getNodes();
		_tenantArea.addAll(interior);
		
		saveInterior();
		
		return interior.size();
	}
	
	public void clearInterior() {
		_tenantArea.clear();
        try {
            getInteriorFile(true); // delete interior file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	
	public IDataNode getSettings() {
		return _dataNode;
	}
	
	private void loadSettings() {
		UUID tenantId = super.getOwnerId();
		if (tenantId == null)
			return;
		
		_tenant = Tenant.add(tenantId, this);
		
		_rentExpiration = null;
		String expiresStr = _dataNode.getString("rent-expiration");
		if (expiresStr != null) {
			SimpleDateFormat format = new SimpleDateFormat();
			try {
				_rentExpiration = format.parse(expiresStr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private File getInteriorFile(boolean deleteIfExists) throws IOException {
		File interiorDir = new File(getDataFolder(), "interiors");
		if (!interiorDir.exists() && !interiorDir.mkdir())
			throw new IOException("Failed to create interior file folder.");
		
		File file = new File(interiorDir, getFilePrefix() + ".bin");
		if (deleteIfExists && file.exists() && !file.delete())
			throw new IOException("Failed to delete interior file.");
		
		return file;
	}
	
	
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
		
		Bukkit.getScheduler().runTaskAsynchronously(RentalRooms.getInstance(), new LoadInterior(this, file));
	}
	
	private void saveInterior() {

        File file;

        try {
            file = getInteriorFile(true);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(RentalRooms.getInstance(), new SaveInterior(this, _tenantArea, file));
	}
	
	private static final class LoadInterior implements Runnable {

		private RentRegion _region;
		private List<Location> _interior;
		private File _file;
		
		public LoadInterior(RentRegion region, File file) {
			_region = region;
			_file = file;
		}
		
		@Override
		public void run() {
			GenericsByteReader reader = null;
			
			try {
				reader = new GenericsByteReader(new FileInputStream(_file));
				
				int fileVersion = reader.getInteger();
				
				if (fileVersion != INTERIOR_FILE_VERSION) {
					Msg.warning("Failed to load interior file because it's not the correct version: {0}", _file.getName());
					return;
				}
				
				reader.getString(); // get region name
				
				String worldName = reader.getString();
				World world = Bukkit.getWorld(worldName);
				
				if (world == null) {
					Msg.warning("Failed to load interior file because the world it's in ({0}) is not loaded: {1}", worldName, _file.getName());
					return;
				}

				reader.getLocation(); // p1
				reader.getLocation(); // p2
				
				int size = reader.getInteger();
				
				_interior = new ArrayList<Location>(size + 2);
				
				for (int i=0; i < size; i++) {
					Location loc = reader.getLocation();
					if (loc != null)
						_interior.add(loc);
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

			Bukkit.getScheduler().scheduleSyncDelayedTask(RentalRooms.getInstance(), new Runnable () {

				@Override
				public void run() {
					_region._tenantArea.clear();
					_region._tenantArea.addAll(_interior);
				}
			});
			
		}
		
	}
	
	
	private static final class SaveInterior implements Runnable {
		
		private RentRegion _region;
		private List<Location> _interior;
		private File _file;
		
		public SaveInterior(RentRegion region, Set<Location> interior, File file) {
			_region = region;
			_interior = new ArrayList<Location>(interior);
			_file = file;
		}

		@Override
		public void run() {
			
			GenericsByteWriter writer = null;
			
			try {
				
				_file.createNewFile();
				
				writer = new GenericsByteWriter(new FileOutputStream(_file));
				
				writer.write(INTERIOR_FILE_VERSION);
				writer.write(_region.getName());
				writer.write(_region.getWorld().getName());
				writer.write(_region.getP1());
				writer.write(_region.getP2());
				writer.write(_interior.size());
				
				for (Location loc : _interior) {
					writer.write(loc);
				}
				
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

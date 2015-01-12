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

import com.jcwhatever.nucleus.utils.Economy;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.DateUtils;
import com.jcwhatever.nucleus.utils.DateUtils.TimeRound;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.bukkit.rental.events.RentPayedEvent;
import com.jcwhatever.bukkit.rental.events.RentPriceChangedEvent;
import com.jcwhatever.bukkit.rental.region.RentRegion;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;
import com.jcwhatever.bukkit.rental.signs.RentalSignHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.List;

public class BillCollector {

	private double _costPerVolume = 1.0D;
	private int _rentCycle = 1; // term units are real life days
	private int _maxAdvanceDays = 3;

	private IDataNode _settings;

	public BillCollector(IDataNode settings) {

		_settings = settings;

		loadSettings();

		Bukkit.getScheduler().scheduleSyncRepeatingTask(RentalRooms.getInstance(), new DueChecker(), 600, 1200);
	}
	
	public int getMaxAdvancedDays() {
	    return _maxAdvanceDays;
	}
	
	public void setMaxAdvanceDays(int maxAdvance) {
	    _maxAdvanceDays = maxAdvance;
	    
	    _settings.set("max-advance-days", maxAdvance);
	    _settings.saveAsync(null);
	}

	public double getCostPerVolume() {
		return _costPerVolume;
	}

	public void setCostPerVolume(double costPerVolume) {

		RentPriceChangedEvent event = RentPriceChangedEvent.callEvent(_costPerVolume, costPerVolume);

		if (event.isCancelled())
			return;

		_costPerVolume = event.getNewAmount();
		_settings.set("cost-per-volume", event.getNewAmount());
		_settings.saveAsync(null);
	}

	public int getRentCycle() {
		return Math.max(1, _rentCycle);
	}

	public void setRentCycle(int rentCycle) {
		_rentCycle = rentCycle;
		_settings.set("rent-cycle", rentCycle);
		_settings.saveAsync(null);
	}

	public double getRentCost(RentRegion region) {
		PreCon.notNull(region);

		return region.getRentSpaceVolume() * _costPerVolume; 
	}

	public boolean canPay(RentRegion region, Player p) {

        double rentAmount = getRentCost(region);

        if (rentAmount == 0)
            return true;

        double playerBalance = Economy.getBalance(p.getUniqueId());
        return playerBalance >= rentAmount;
    }

	
	public boolean canPayAdvance(RentRegion region) {
	    
	    Date expires = region.getExpirationDate();
	    if (expires == null)
	        throw new IllegalStateException("Expiration date is null.");
	    
	    long hours = DateUtils.getDeltaHours(new Date(), expires, TimeRound.ROUND_DOWN);
	    
	    int days = (int)Math.ceil((double)hours / 24);
	    
	    return days < _maxAdvanceDays + 1;
    }


	public boolean charge(RentRegion region, Player p) {

		double rentAmount = getRentCost(region);

		if (rentAmount == 0)
			return true;

		if (!canPay(region, p))
			return false;

		Economy.withdraw(p.getUniqueId(), rentAmount);

		region.setPayed();
		
		RentPayedEvent.callEvent(region, rentAmount);

		return true;
	}

	public String getFormattedRentAmount(RentRegion region) {

		double rentAmount = getRentCost(region);

		return Economy.getCurrency().format(rentAmount);
	}
	
	private void loadSettings() {
		_costPerVolume = _settings.getDouble("cost-per-volume");
		_rentCycle = _settings.getInteger("rent-cycle");
		_maxAdvanceDays = _settings.getInteger("max-advance-days", _maxAdvanceDays);
	}


	private class DueChecker implements Runnable {

		RentalSignHandler signHandler = RentalRooms.getInstance().getSignHandler();
		Date nextUpdate = new Date();
		
		@Override
		public void run() {
			
			if (new Date().compareTo(nextUpdate) < 0) {
				return;
			}
			
			nextUpdate = org.apache.commons.lang.time.DateUtils.addHours(nextUpdate, 1);
			
			RentRegionManager manager = RentalRooms.getInstance().getRegionManager();

			List<RentRegion> regions = manager.getRegions();

			Date now = new Date();

			for (RentRegion region : regions) {
				if (!region.hasTenant())
					continue;

				Date expires = region.getExpirationDate();
				if (expires == null)
					continue;
				
				Tenant tenant = region.getTenant();

				// Check for eviction
				if (now.compareTo(expires) > 0) { // overdue, evict
					
					region.evict();

					Msg.tellImportant(tenant.getPlayerID(), "rent_status_" + region.getName(), "You've been evicted from rent region '{0}'.", region.getName());

					continue;
				}

				// Check for warning 
				Date warningDate = org.apache.commons.lang.time.DateUtils.addHours(expires, -24);
				
				if (now.compareTo(warningDate) >= 0) {
					
					Player p = tenant.getPlayer();
					if (p != null) {
						
						long hoursLeft = DateUtils.getDeltaHours(now, expires, TimeRound.ROUND_DOWN);
						
						if (hoursLeft <= 1) {
							long minutesLeft = DateUtils.getDeltaMinutes(now, expires, TimeRound.ROUND_DOWN);
							Msg.tell(p, "{0} Rent will be due for '{1}' in {2} minutes.", getFormattedRentAmount(region), region.getName(), minutesLeft);
						}
						else {
							Msg.tell(p, "{0} Rent will be due for '{1}' in {2} hours.", getFormattedRentAmount(region), region.getName(), hoursLeft);
						}
					}
					
					
				}
				
				signHandler.updateTimeLeft(region);
			}

		}

	}

}

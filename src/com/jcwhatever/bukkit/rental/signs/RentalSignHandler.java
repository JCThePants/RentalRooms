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


package com.jcwhatever.bukkit.rental.signs;

import com.jcwhatever.generic.utils.EconomyUtils;
import com.jcwhatever.generic.utils.player.PlayerUtils;
import com.jcwhatever.generic.signs.SignContainer;
import com.jcwhatever.generic.signs.SignHandler;
import com.jcwhatever.generic.utils.DateUtils;
import com.jcwhatever.generic.utils.DateUtils.TimeRound;
import com.jcwhatever.generic.utils.PreCon;
import com.jcwhatever.bukkit.rental.BillCollector;
import com.jcwhatever.bukkit.rental.Msg;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.Tenant;
import com.jcwhatever.bukkit.rental.events.RentMoveInEvent;
import com.jcwhatever.bukkit.rental.events.RentMoveOutEvent;
import com.jcwhatever.bukkit.rental.events.RentPayedEvent;
import com.jcwhatever.bukkit.rental.events.RentPriceChangedEvent;
import com.jcwhatever.bukkit.rental.region.RentRegion;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Date;
import java.util.List;

public class RentalSignHandler extends SignHandler {

	private RentRegionManager _regionManager;
    private RentalRooms _plugin;

	public RentalSignHandler () {
		_regionManager = RentalRooms.getInstance().getRegionManager();

        _plugin = RentalRooms.getInstance();
		Bukkit.getPluginManager().registerEvents(new EventListener(), RentalRooms.getInstance());
	}

    @Override
    public Plugin getPlugin() {
        return _plugin;
    }

    @Override
	public String getName() {
		return "Rental";
	}

    @Override
    public String getDescription() {
        return "An information display sign for a rental room. <regionName> is the name of the rental room region.";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                "Rental",
                "<regionName>",
                "--reserved--",
                "--reserved--"
        };
    }

    @Override
	public String getHeaderPrefix() {
		return ChatColor.DARK_BLUE.toString();
	}

	@Override
	protected boolean onSignClick(Player p, SignContainer sign) {

		String regionName = sign.getRawLine(1);

		RentRegion region = _regionManager.getRegion(regionName);

		if (region == null)
			return false;

		if (region.hasTenant()) {

			Tenant tenant = region.getTenant();
			
			if (p.getUniqueId().equals(tenant.getPlayerID())) {
				BillCollector billCollector = RentalRooms.getInstance().getBillCollector();
				Msg.tell(p, "{WHITE}Type {YELLOW}'/rent pay {0}'{WHITE} to pay your rent in the amount of {1}. Next payment due by {2}.", region.getName(), billCollector.getFormattedRentAmount(region), region.getFormattedExpiration());
			}
			else {
				Msg.tell(p, "Currently being rented by {0}.", tenant.getPlayerName());
			}

		}
		else {
			Msg.tell(p, "{WHITE}Type {YELLOW}'/rent movein'{WHITE} while standing in the rent area to move in.");
		}

		return false;
	}

	@Override
	public boolean onSignChange(Player p, SignContainer sign) {
		if (!p.isOp())
			return false;

		String regionName = sign.getRawLine(1);
		RentRegion region = _regionManager.getRegion(regionName);

		if (region == null)
			return false;

		setSignOwnerForRegion(region, sign);

		return true;
	}

	@Override
	public boolean onSignBreak(Player p, SignContainer sign) {
		String regionName = sign.getRawLine(1);

		RentRegion region = _regionManager.getRegion(regionName);

        return region == null || p.isOp() && region.isEditModeOn();
    }

	@Override
	public void onSignLoad(SignContainer sign) {
		// do nothing

	}
	
	public void updateTimeLeft(RentRegion region) {
		
		PreCon.notNull(region);
		
		// if (!region.hasTenant())
		//	return;
		
		Date expires = region.getExpirationDate();
		
		if (expires == null)
			expires = new Date();
		
		long hoursLeft = DateUtils.getDeltaHours(new Date(), expires, TimeRound.ROUND_DOWN);
		
		long daysLeft = hoursLeft / 24;

		List<SignContainer> signs = _plugin.getSignManager().getSigns(getName());
        if (signs == null)
            return;
		
		for (SignContainer sign : signs) {
			if (isRegionSign(region, sign.getSign())) {
				
				if (hoursLeft <= 1) {
					sign.setLine(3, ChatColor.RED + "Expiring soon");
				}
				else {
					sign.setLine(3, hoursLeft <= 24 ? hoursLeft + " hours" : daysLeft + " days " + (hoursLeft % 24) + " hrs");
				}
				sign.update();

			}
		}
	}


	private boolean isRegionSign(RentRegion region, Sign sign) {
		String regionName = sign.getLine(1);

		return _regionManager.getRegion(regionName) == region;
	}

	private void updateSignOwners(RentRegion region) {

		List<SignContainer> signs = _plugin.getSignManager().getSigns(getName());
        if (signs == null)
            return;

		for (SignContainer sign : signs) {
			if (region == null || isRegionSign(region, sign.getSign())) {

				RentRegion signRegion = region != null 
						? region
								: _regionManager.getRegion(sign.getLine(1));

				if (signRegion == null)
					continue;

				setSignOwnerForRegion(signRegion, sign);

				sign.update();
			}
		}
	}

	private void updateSignPrices(double newAmount) {

		List<SignContainer> signs = _plugin.getSignManager().getSigns(getName());
        if (signs == null)
            return;

		for (SignContainer sign : signs) {
			
			RentRegion signRegion = _regionManager.getRegion(sign.getLine(1));

			if (signRegion == null)
				continue;
			
			String formatted = "";
			
			if (!signRegion.hasTenant()) {
				double newRegionAmount = newAmount * signRegion.getRentSpaceVolume();
				formatted = EconomyUtils.formatAmount(newRegionAmount);
			}
			
			sign.setLine(3, formatted);
			sign.update();
		}
	}

	private void setSignOwnerForRegion(RentRegion region, SignContainer sign) {
		PreCon.notNull(region);
		
		BillCollector billCollector = RentalRooms.getInstance().getBillCollector();

		Player tenant = region.hasTenant() 
				? PlayerUtils.getPlayer(region.getTenant().getPlayerID())
				: null;

		String tenantName = region.hasTenant() 
				? tenant != null 
					? tenant.getName()
					: "[unknown]" 
				: null;

		sign.setLine(2, tenantName != null
				        ? ChatColor.DARK_BLUE + tenantName
				        : ChatColor.GREEN + "For Rent");
		
		sign.setLine(3, region.hasTenant()
				        ? ""
				        :  billCollector.getFormattedRentAmount(region));
	}



	private class EventListener implements Listener {

		@EventHandler
		private void onRentMoveIn(RentMoveInEvent event) {
			updateSignOwners(event.getRentRegion());
		}

		@EventHandler
		private void onRentMoveOut(RentMoveOutEvent event) {
			updateSignOwners(event.getRentRegion());
		}

		@EventHandler
        private void onPriceChanged(RentPriceChangedEvent event) {
            updateSignPrices(event.getNewAmount());
        }
		
		@EventHandler
        private void onRentPayed(RentPayedEvent event) {
            updateTimeLeft(event.getRentRegion());
        }
		
	}




}

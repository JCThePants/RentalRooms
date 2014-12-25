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


package com.jcwhatever.bukkit.rental.commands.users;

import com.jcwhatever.generic.commands.AbstractCommand;
import com.jcwhatever.generic.commands.CommandInfo;
import com.jcwhatever.generic.commands.arguments.CommandArguments;
import com.jcwhatever.generic.commands.exceptions.CommandException;
import com.jcwhatever.bukkit.rental.BillCollector;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.region.RentRegion;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.UUID;

@CommandInfo(
		command="movein", 
		description="Move in to the rental unit you're standing in.",
		permissionDefault=PermissionDefault.TRUE)

public class MoveInCommand extends AbstractCommand {
	
	@Override
	public void execute(CommandSender sender, CommandArguments args) throws CommandException {

		CommandException.assertNotConsole(this, sender);
				
		Player p = (Player)sender;
		
		RentRegionManager regionManager = RentalRooms.getInstance().getRegionManager();
		
		RentRegion region = regionManager.getRegion(p.getLocation());
		if (region == null) {
			tellError(p, "You're not standing in a rental unit. Stand in the unit you want to rent before running the movein command.");
			return; // finish
		}
		
		if (region.hasTenant()) {
			
			if (region.getTenant().equals(p)) {
				tellError(p, "You're already renting this unit.");
				return; // finish
			}
			
			tellError(p, "You can't rent this unit because it already has a tenant.");
			return; // finish
		}
		
		BillCollector billCollector = RentalRooms.getInstance().getBillCollector();
		
		if (!billCollector.canPay(region, p)) {
			tellError(p, "You can't afford to rent this unit.");
			return; // finish
		}
		
		
		region.setTenant(p);
		
		if (!billCollector.charge(region, p)) {
		    region.setTenant((UUID)null);
			tellError(p, "Failed to pay for rental '{0}'.", region.getName());
			return; // finish
		}
				
		tellSuccess(p, "You are now the tenant of this rental. {0} have been taken from your account and you will be expected to pay the rent again in {1} real life days.", billCollector.getFormattedRentAmount(region), billCollector.getRentCycle());
	}
}


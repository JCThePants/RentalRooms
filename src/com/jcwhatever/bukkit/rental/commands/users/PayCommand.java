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

import com.jcwhatever.nucleus.commands.AbstractCommand;
import com.jcwhatever.nucleus.commands.CommandInfo;
import com.jcwhatever.nucleus.commands.arguments.CommandArguments;
import com.jcwhatever.nucleus.commands.exceptions.CommandException;
import com.jcwhatever.bukkit.rental.BillCollector;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.region.RentRegion;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

@CommandInfo(
		command="pay", 
		staticParams={"rentalName"},
		description="Pay the rent on the specified rental region.",
		permissionDefault=PermissionDefault.TRUE)

public class PayCommand extends AbstractCommand {
	
	@Override
	public void execute(CommandSender sender, CommandArguments args) throws CommandException {

		CommandException.assertNotConsole(this, sender);
				
		Player p = (Player)sender;
		
		String rentalName = args.getName("rentalName");
		
		RentRegionManager regionManager = RentalRooms.getInstance().getRegionManager();
		
		RentRegion region = regionManager.getRegion(rentalName);
		if (region == null) {
			tellError(p, "The rental region '{0}' was not found.", rentalName);
			return; // finish
		}
		
		if (!region.hasTenant() || !region.getTenant().getPlayerID().equals(p.getUniqueId())) {
			tellError(p, "You aren't the tenant of rental region '{0}'.", region.getName());
			return; // finish
		}
		
		BillCollector billCollector = RentalRooms.getInstance().getBillCollector();
		
		if (!billCollector.canPay(region, p)) {
		    tellError(p, "You can't afford to pay {0}. Please check your balance.", billCollector.getFormattedRentAmount(region));
            return; // finish
		}
		
		if (!billCollector.canPayAdvance(region)) {
		    tellError(p, "You cannot pay more than {0} days in advance.", billCollector.getMaxAdvancedDays());
            return; // finish
		}
		
		if (!billCollector.charge(region, p)) {
			tellError(p, "Failed to pay for rental '{0}'.", region.getName());
			return; // finish
		}
		
		tellSuccess(p, "Payed {0} for rental '{1}'. Next payment due {2}.", billCollector.getFormattedRentAmount(region), region.getName(), region.getFormattedExpiration());
	}
	
}


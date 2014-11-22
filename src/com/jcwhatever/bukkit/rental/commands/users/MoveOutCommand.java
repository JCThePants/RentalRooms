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

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.CommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException.CommandSenderType;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;
import com.jcwhatever.bukkit.rental.region.RentRegion;

@CommandInfo(
		command="moveout", 
		staticParams={"rentalName"},
		usage="/rent moveout [rentalName]",
		description="Move out of the rental unit you're standing in or specify.",
		permissionDefault=PermissionDefault.TRUE)

public class MoveOutCommand extends AbstractCommand {

	@Override
	public void execute(CommandSender sender, CommandArguments args)
	        throws InvalidValueException, InvalidCommandSenderException {

	    InvalidCommandSenderException.check(sender, CommandSenderType.PLAYER);

		Player p = (Player)sender;

		RentRegionManager regionManager = RentalRooms.getInstance().getRegionManager();
		
		
		RentRegion region;
		if (args.hasString("rentalName")) {
			String rentalName = args.getString("rentalName");
			region = regionManager.getRegion(rentalName);
			if (region == null) {
				tellError(p, "Rental region '{0}' not found.", rentalName);
				return; // finish
			}
		}
		else {
			region = regionManager.getRegion(p.getLocation());
			if (region == null) {
				tellError(p, "Your not standing in a rental region.");
				return; // finish
			}
		}
		
		if (!region.hasTenant() || !region.getTenant().equals(p)) {
			tellError(p, "You can't move out because you aren't the tenant.");
			return; // finish
		}
		
		region.evict();
		
		tellSuccess(p, "You have moved out from '{0}'", region.getName());
	}
	
}
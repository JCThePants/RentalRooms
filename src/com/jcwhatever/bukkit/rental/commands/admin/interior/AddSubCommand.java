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


package com.jcwhatever.bukkit.rental.commands.admin.interior;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException.CommandSenderType;
import com.jcwhatever.bukkit.generic.messaging.Messenger;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;
import com.jcwhatever.bukkit.rental.region.RentRegion;

@ICommandInfo(
		parent="interior", 
		command="add",
		staticParams={"rentalName"},
		usage="/rent interior add <rentalName>", 
		description="Searches the interior of a building starting from where you are standing to get the tenant editable interior.")

public class AddSubCommand extends AbstractCommand {
	
	@Override
    public void execute(CommandSender sender, CommandArguments args)
            throws InvalidValueException, InvalidCommandSenderException {
        
	    InvalidCommandSenderException.check(sender, CommandSenderType.PLAYER, 
	            "Console has no location.");
		
		String rentalName = args.getName("rentalName");
		
		Player p = (Player)sender;
		
		Location start = p.getLocation();
		
		RentRegionManager regionManager = RentalRooms.getInstance().getRegionManager();
		
		RentRegion region = regionManager.getRegion(rentalName);
		if (region == null) {
			Messenger.tell(RentalRooms.getInstance(), p, "A rental region with the name '" + rentalName + "' was not found.");
			return; // finish
		}
		
		if (!region.isDefined()) {
			Messenger.tell(RentalRooms.getInstance(), p, "Rental region '" + rentalName + "' is not defined yet.");
			return; // finish
		}
		
		if (!region.contains(start)) {
			Messenger.tell(RentalRooms.getInstance(), p, "You must be standing inside the rental region.");
			return; // finish
		}
		
		int added = region.addInterior(start);
		
		
		Messenger.tell(RentalRooms.getInstance(), p, "Added " + added + " new interior locations to rental region '" + region.getName() + "'.");
    }
	
}

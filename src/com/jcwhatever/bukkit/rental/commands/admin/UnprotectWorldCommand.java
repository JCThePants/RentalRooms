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


package com.jcwhatever.bukkit.rental.commands.admin;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.CommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidArgumentException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException.CommandSenderType;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;

@CommandInfo(
		command="unprotectworld", 
		staticParams={"worldName=$default"},
		usage="/rent unprotectworld [worldName]",
		description="Remove world from protection.")

public class UnprotectWorldCommand extends AbstractCommand {
	
	@Override
	public void execute(CommandSender sender, CommandArguments args)
	        throws InvalidArgumentException, InvalidCommandSenderException {

		String worldName;

		if (args.getString("worldName").equals("$default")) {

		    InvalidCommandSenderException.check(sender, CommandSenderType.PLAYER,
		            "Console has no location. Specify world name.");
		    			
			worldName = ((Player)sender).getWorld().getName(); 
		}
		else {
			worldName = args.getName("worldName");
		}
		
		
		RentRegionManager regionManager = RentalRooms.getInstance().getRegionManager();
		
		boolean result = regionManager.removeProtectedWorld(worldName); 
		
		if (!result) {
			World world = Bukkit.getWorld(worldName);
			
			if (world == null) {
				tellError(sender, "A world with the name '{0}' was not found.", worldName);
				return; // finish
			}
			else {
				result = regionManager.removeProtectedWorld(world.getName());
			}
		}
		
		
		if (!result) {
			tellError(sender, "Failed to remove world '{0}'.", worldName);
			return; // finish
		}
		
		tellSuccess(sender, "World '{0}' is no longer a protected world.", worldName);
	}
}

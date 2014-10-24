package com.jcwhatever.bukkit.rental.commands.admin;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException.CommandSenderType;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;

@ICommandInfo(
		command="unprotectworld", 
		staticParams={"worldName=$default"},
		usage="/rent unprotectworld [worldName]",
		description="Remove world from protection.")

public class UnprotectWorldCommand extends AbstractCommand {
	
	@Override
	public void execute(CommandSender sender, CommandArguments args)
	        throws InvalidValueException, InvalidCommandSenderException {

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

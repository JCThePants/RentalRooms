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
		command="protectworld", 
		staticParams={"worldName=$default"},
		usage="/rent protectworld [worldName]",
		description="Prevent editing entire world you are in or specify a world.")

public class ProtectWorldCommand extends AbstractCommand {
	
	@Override
	public void execute(CommandSender sender, CommandArguments args)
	        throws InvalidValueException, InvalidCommandSenderException {
		
		String worldName = null;
		
		if (args.getString("worldName").equals("$default")) {
			
		    InvalidCommandSenderException.check(sender, CommandSenderType.PLAYER, 
		            "Console has no location. Specify world name.");
						
			worldName = ((Player)sender).getWorld().getName(); 
		}
		else {
			worldName = args.getName("worldName");
		}
		
		
		RentRegionManager regionManager = RentalRooms.getInstance().getRegionManager();
		
		World world = Bukkit.getWorld(worldName);
		
		if (world == null) {
			tellError(sender, "A world with the name '{0}' was not found.", worldName);
			return; // finish
		}
		
		if (!regionManager.addProtectedWorld(world)) {
			tellError(sender, "Failed to add world '{0}'.", world.getName());
			return; // finish
		}
		
		tellSuccess(sender, "World '{0}' is now a protected world.", world.getName());
	}
}

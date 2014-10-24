package com.jcwhatever.bukkit.rental.commands.users;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException.CommandSenderType;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;
import com.jcwhatever.bukkit.rental.region.RentRegion;

@ICommandInfo(
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
package com.jcwhatever.bukkit.rental.commands.admin;

import org.bukkit.command.CommandSender;

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
		command="editmode", 
		staticParams={"rentalName", "on|off"},
		usage="/rent editmode <rentalName> <on|off>",
		description="Allow editing of protected blocks in specified rental region.")

public class EditModeCommand extends AbstractCommand {
	
	@Override
	public void execute(CommandSender sender, CommandArguments args)
	        throws InvalidValueException, InvalidCommandSenderException {
		
	    InvalidCommandSenderException.check(sender, CommandSenderType.PLAYER);
		
		String rentalName = args.getName("rentalName");
		boolean isEdit = args.getBoolean("on|off");
		
		RentRegionManager regionManager = RentalRooms.getInstance().getRegionManager();
		
		RentRegion region = regionManager.getRegion(rentalName);
		if (region == null) {
			tellError(sender, "A rental region with the name '{0}' was not found.", rentalName);
			return; // finish
		}
		
		region.setIsEditModeOn(isEdit);
		
		tellEnabled(sender, "Edit mode in rental region '{0}' {e}.", isEdit, rentalName);
	}
}

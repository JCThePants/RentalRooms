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

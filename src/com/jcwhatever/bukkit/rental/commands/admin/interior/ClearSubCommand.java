package com.jcwhatever.bukkit.rental.commands.admin.interior;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.messaging.Messenger;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;
import com.jcwhatever.bukkit.rental.region.RentRegion;

@ICommandInfo(
		parent="interior", 
		command="clear",
		staticParams={"rentalName"},
		usage="/rent interior clear <rentalName>", 
		description="Clear stored interior locations from the specified rental region.")

public class ClearSubCommand extends AbstractCommand {
	
	@Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {
        	    
		String rentalName = args.getName("rentalName");
		
		Player p = (Player)sender;
		
		RentRegionManager regionManager = RentalRooms.getInstance().getRegionManager();
		
		RentRegion region = regionManager.getRegion(rentalName);
		if (region == null) {
			Messenger.tell(RentalRooms.getInstance(), p, "A rental region with the name '" + rentalName + "' was not found.");
			return; // finish
		}
		
		region.clearInterior();
				
		Messenger.tell(RentalRooms.getInstance(), p, "Cleared interior locations from rental region '" + region.getName() + "'.");
    }

}


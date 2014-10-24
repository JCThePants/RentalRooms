package com.jcwhatever.bukkit.rental.commands.admin;

import org.bukkit.command.CommandSender;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.region.RentRegion;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;


@ICommandInfo(
        command="del", 
        staticParams="rentalName",
        usage="/rent del <rentalName>",
        description="Remove a rental room.")

public class DelCommand extends AbstractCommand {
    
    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {
        
        String rentalName = args.getName("rentalName");
        
        RentRegionManager regionManager = RentalRooms.getInstance().getRegionManager();
        
        RentRegion region = regionManager.getRegion(rentalName);
        if (region == null) {
            tellError(sender, "A rental region named '{0}' was not found.", rentalName);
            return; // finish
        }
        
        if (!regionManager.removeRegion(rentalName)) {
            tellError(sender, "Failed to remove rental region.");
        }
        
        tellSuccess(sender, "Rental region '{0}' removed.", rentalName);
    }
    

}

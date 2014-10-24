package com.jcwhatever.bukkit.rental.commands.admin;

import com.jcwhatever.bukkit.generic.regions.RegionSelection;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException.CommandSenderType;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.region.RentRegion;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;


@ICommandInfo(
        command="setregion", 
        staticParams="rentalName",
        usage="/rent setregion <rentalName>",
        description="Set the a rental rooms region using your current world edit cuboid selection.")

public class SetRegionCommand extends AbstractCommand {
    
    @Override
    public void execute(CommandSender sender, CommandArguments args)
            throws InvalidValueException, InvalidCommandSenderException {
        
        InvalidCommandSenderException.check(sender, CommandSenderType.PLAYER,
                "Console cannot select regions.");
        
        if (!isWorldEditInstalled(sender))
            return; // finish
        
        
        String rentalName = args.getName("rentalName");
        
        Player p = (Player)sender;
        
        RegionSelection selection = getWorldEditSelection(p);
        if (selection == null)
            return; // finish
        
        Location p1 = selection.getP1();
        Location p2 = selection.getP2();
                
        if (p1 == null || p2 == null) {
            tellError(p, "World edit selection incomplete. Please select both points of the cuboid area.");
            return; // finish
        }
        
        RentRegionManager regionManager = RentalRooms.getInstance().getRegionManager();
        
        RentRegion region = regionManager.getRegion(rentalName);
        if (region == null) {
            tellError(p, "A rental with the name '{0}' was not found.", rentalName);
            return; // finish
        }
        
        region.setCoords(p1, p2);
        region.clearInterior();
        
        tellSuccess(p, "Rental region '{0}' set. Interior cleared.", rentalName);
    }
    

}
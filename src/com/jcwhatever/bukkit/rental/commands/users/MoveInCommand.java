package com.jcwhatever.bukkit.rental.commands.users;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException.CommandSenderType;
import com.jcwhatever.bukkit.rental.BillCollector;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;
import com.jcwhatever.bukkit.rental.region.RentRegion;

@ICommandInfo(
		command="movein", 
		usage="/rent movein",
		description="Move in to the rental unit you're standing in.",
		permissionDefault=PermissionDefault.TRUE)

public class MoveInCommand extends AbstractCommand {
	
	@Override
	public void execute(CommandSender sender, CommandArguments args)
	        throws InvalidValueException, InvalidCommandSenderException {
		
	    InvalidCommandSenderException.check(sender, CommandSenderType.PLAYER);
				
		Player p = (Player)sender;
		
		RentRegionManager regionManager = RentalRooms.getInstance().getRegionManager();
		
		RentRegion region = regionManager.getRegion(p.getLocation());
		if (region == null) {
			tellError(p, "You're not standing in a rental unit. Stand in the unit you want to rent before running the movein command.");
			return; // finish
		}
		
		if (region.hasTenant()) {
			
			if (region.getTenant().equals(p)) {
				tellError(p, "You're already renting this unit.");
				return; // finish
			}
			
			tellError(p, "You can't rent this unit because it already has a tenant.");
			return; // finish
		}
		
		BillCollector billCollector = RentalRooms.getInstance().getBillCollector();
		
		if (!billCollector.canPay(region, p)) {
			tellError(p, "You can't afford to rent this unit.");
			return; // finish
		}
		
		
		region.setTenant(p);
		
		if (!billCollector.charge(region, p)) {
		    region.setTenant((UUID)null);
			tellError(p, "Failed to pay for rental '{0}'.", region.getName());
			return; // finish
		}
				
		tellSuccess(p, "You are now the tenant of this rental. {0} have been taken from your account and you will be expected to pay the rent again in {1} real life days.", billCollector.getFormattedRentAmount(region), billCollector.getRentCycle());
	}
}


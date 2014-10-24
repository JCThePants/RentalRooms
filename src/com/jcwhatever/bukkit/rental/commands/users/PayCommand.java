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
import com.jcwhatever.bukkit.rental.BillCollector;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.region.RentRegion;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;

@ICommandInfo(
		command="pay", 
		staticParams={"rentalName"},
		usage="/rent pay <rentalName>",
		description="Pay the rent on the specified rental region.",
		permissionDefault=PermissionDefault.TRUE)

public class PayCommand extends AbstractCommand {
	
	@Override
	public void execute(CommandSender sender, CommandArguments args)
	        throws InvalidValueException, InvalidCommandSenderException {
		
	    InvalidCommandSenderException.check(sender, CommandSenderType.PLAYER);
				
		Player p = (Player)sender;
		
		String rentalName = args.getName("rentalName");
		
		RentRegionManager regionManager = RentalRooms.getInstance().getRegionManager();
		
		RentRegion region = regionManager.getRegion(rentalName);
		if (region == null) {
			tellError(p, "The rental region '{0}' was not found.", rentalName);
			return; // finish
		}
		
		if (!region.hasTenant() || !region.getTenant().getPlayerID().equals(p.getUniqueId())) {
			tellError(p, "You aren't the tenant of rental region '{0}'.", region.getName());
			return; // finish
		}
		
		BillCollector billCollector = RentalRooms.getInstance().getBillCollector();
		
		if (!billCollector.canPay(region, p)) {
		    tellError(p, "You can't afford to pay {0}. Please check your balance.", billCollector.getFormattedRentAmount(region));
            return; // finish
		}
		
		if (!billCollector.canPayAdvance(region)) {
		    tellError(p, "You cannot pay more than {0} days in advance.", billCollector.getMaxAdvancedDays());
            return; // finish
		}
		
		if (!billCollector.charge(region, p)) {
			tellError(p, "Failed to pay for rental '{0}'.", region.getName());
			return; // finish
		}
		
		tellSuccess(p, "Payed {0} for rental '{1}'. Next payment due {2}.", billCollector.getFormattedRentAmount(region), region.getName(), region.getFormattedExpiration());
	}
	
}


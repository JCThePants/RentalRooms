package com.jcwhatever.bukkit.rental.commands.users.friends;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException.CommandSenderType;
import com.jcwhatever.bukkit.generic.player.PlayerHelper;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.region.RentRegion;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;

@ICommandInfo(
		parent="friends",
		command="add", 
		staticParams={"rentalName", "playerName"},
		usage="/rent friends add <rentalName> <playerName>",
		description="Add a friend who has permission to interact with the specified rented region.",
		permissionDefault=PermissionDefault.TRUE)

public class AddSubCommand extends AbstractCommand {
	
	@Override
	public void execute(CommandSender sender, CommandArguments args)
	        throws InvalidValueException, InvalidCommandSenderException {
		
	    InvalidCommandSenderException.check(sender, CommandSenderType.PLAYER);
				
		Player p = (Player)sender;
		
		String regionName = args.getName("rentalName");
		String playerName = args.getName("playerName");
		
		RentRegionManager regionManager = RentalRooms.getInstance().getRegionManager();
		
		RentRegion region = regionManager.getRegion(regionName);
		if (region == null) {
			tellError(p, "Rental '{0}' was not found.", regionName);
			return; // finish
		}
		
		if (!region.hasTenant() || !region.getTenant().equals(p)) {
			tellError(p, "You are not the tenant of rental '{0}'.", regionName);
			return; // finish
		}
		
		Player friend = PlayerHelper.getPlayer(playerName);
		if (friend == null) {
			tellError(p, "'{0}' was not found or is not on the server.", playerName);
			return; // finish
		}
		
		region.getFriendManager().addFriend(friend);
						
		tellSuccess(p, "'{0}' has been added as a friend.", friend.getName());
	}
}
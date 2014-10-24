package com.jcwhatever.bukkit.rental.commands.users.friends;

import java.util.List;

import com.jcwhatever.bukkit.generic.messaging.ChatPaginator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException.CommandSenderType;
import com.jcwhatever.bukkit.rental.Lang;
import com.jcwhatever.bukkit.generic.utils.TextUtils.FormatTemplate;
import com.jcwhatever.bukkit.rental.Friend;
import com.jcwhatever.bukkit.rental.Msg;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.region.RentRegion;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;

@ICommandInfo(
		parent="friends",
		command="list", 
		staticParams={ "rentalName", "page=1"},
		usage="/rent friends list <rentalName> [page]",
		description="List all friends in the specified rental.",
		permissionDefault=PermissionDefault.TRUE)

public class ListSubCommand extends AbstractCommand {
	
	@Override
	public void execute(CommandSender sender, CommandArguments args)
	        throws InvalidValueException, InvalidCommandSenderException {

	    InvalidCommandSenderException.check(sender, CommandSenderType.PLAYER);
				
		Player p = (Player)sender;
		
		String rentalName = args.getName("rentalName");
		int page = args.getInt("page");
		
		RentRegionManager regionManager = RentalRooms.getInstance().getRegionManager();
		
		RentRegion region = regionManager.getRegion(rentalName);
		if (region == null) {
		    String message = Lang.get("Rental '{0}' was not found.", rentalName);
			tellError(p, message);
			return; // finish
		}
		
		if (!region.hasTenant() || !region.getTenant().equals(p)) {
		    String message = Lang.get("You are not the tenant of rental '{0}'.", rentalName);
			tellError(p, message);
			return; // finish
		}
		
		List<Friend> friends = region.getFriendManager().getFriends();
				
		String paginTitle = Lang.get("Friends for '{0}'", region.getName());
		ChatPaginator pagin = Msg.getPaginator(paginTitle);
		
		for (Friend friend : friends) {
			
			pagin.add(friend.getPlayerName());
		}
		
		pagin.show(sender, page, FormatTemplate.ITEM);
	}
}

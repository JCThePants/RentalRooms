package com.jcwhatever.bukkit.rental.commands.users;

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
import com.jcwhatever.bukkit.rental.BillCollector;
import com.jcwhatever.bukkit.rental.Msg;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.Tenant;
import com.jcwhatever.bukkit.rental.region.RentRegion;

@ICommandInfo(
		command="list", 
		staticParams={"page=1"},
		usage="/rent list [page]",
		description="Get a list of your rented regions.",
		permissionDefault=PermissionDefault.TRUE)

public class ListCommand extends AbstractCommand {

	@Override
	public void execute(CommandSender sender, CommandArguments args)
	        throws InvalidValueException, InvalidCommandSenderException {

	    InvalidCommandSenderException.check(sender, CommandSenderType.PLAYER);

		Player p = (Player)sender;

		int page = args.getInt("page");

		BillCollector billCollector = RentalRooms.getInstance().getBillCollector();
		Tenant tenant = Tenant.get(p);

		String paginTitle = Lang.get("My Rents");
		ChatPaginator pagin = Msg.getPaginator(paginTitle);
		
		String noneLabel = Lang.get("<none>");
		
		if (tenant == null) {
			pagin.addFormatted(FormatTemplate.RAW, noneLabel);			
		}
		else {
			List<RentRegion> regions = tenant.getRentRegions();
			if (regions.size() == 0) {
				pagin.addFormatted(FormatTemplate.RAW, noneLabel);
			}
			else {

			    String desc = Lang.get("{0} due by {1}");
				for (RentRegion region : regions) {
					pagin.add(region.getName(), desc, billCollector.getFormattedRentAmount(region), region.getFormattedExpiration());
				}
			}
		}

		pagin.show(sender, page, FormatTemplate.DEFINITION);
	}

}

package com.jcwhatever.bukkit.rental.commands.admin;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.messaging.ChatPaginator;
import com.jcwhatever.bukkit.generic.utils.TextUtils.FormatTemplate;
import com.jcwhatever.bukkit.rental.Lang;
import com.jcwhatever.bukkit.rental.Msg;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.region.RentRegion;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;
import org.bukkit.command.CommandSender;

import java.util.List;

@ICommandInfo(
		command="listall", 
		staticParams={"page=1"},
		usage="/rent listall [page]",
		description="List all rental regions.")

public class ListAllCommand extends AbstractCommand {
	
	@Override
	public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {
		
		int page = args.getInt("page");
		
		RentRegionManager regionManager = RentalRooms.getInstance().getRegionManager();
		
		
		List<RentRegion> regions = regionManager.getRegions();
		
		String paginTitle = Lang.get("Rental Regions");
		ChatPaginator pagin = Msg.getPaginator(paginTitle);
		
		String noTenantLabel = Lang.get("<no tenant>");
		for (RentRegion region : regions) {
			pagin.add(region.getName(), region.hasTenant() ? region.getTenant().getPlayerName() : noTenantLabel);
		}
		
		pagin.show(sender, page, FormatTemplate.ITEM);
	}
}

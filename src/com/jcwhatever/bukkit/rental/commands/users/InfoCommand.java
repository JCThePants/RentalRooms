/* This file is part of RentalRooms for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package com.jcwhatever.bukkit.rental.commands.users;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.CommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidArgumentException;
import com.jcwhatever.bukkit.generic.messaging.ChatPaginator;
import com.jcwhatever.bukkit.rental.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.utils.text.TextUtils.FormatTemplate;
import com.jcwhatever.bukkit.rental.BillCollector;
import com.jcwhatever.bukkit.rental.Msg;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.Tenant;
import com.jcwhatever.bukkit.rental.region.RentRegion;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

@CommandInfo(
		command="info", 
		staticParams={"rentalName", "page=1"},
		usage="/rent info <rentalName> [page]",
		description="Get info about the specified rental region.",
		permissionDefault=PermissionDefault.TRUE)

public class InfoCommand extends AbstractCommand {

    @Localizable static final String _RENTAL_NOT_FOUND = "Rental region '{0}' not found.";
    @Localizable static final String _PAGINATOR_TITLE = "Rent Info: {0}";
    @Localizable static final String _LABEL_NONE = "<none>";
    @Localizable static final String _LABEL_VACANT = "<vacant>";
    @Localizable static final String _LABEL_BLOCKS = "blocks";
    @Localizable static final String _LABEL_TENANT = "Tenant";
    @Localizable static final String _LABEL_PRICE = "Price";
    @Localizable static final String _LABEL_VOLUME = "Volume";
    @Localizable static final String _YOUR_RENTALS = "Your Rentals:";
    @Localizable static final String _DESCRIPTION = "{0} every {1} minecraft days";
    @Localizable static final String _STANDING_IN = "Standing in";
    @Localizable static final String _NO_REGION_HERE = "No rental region here.";

	@Override
	public void execute(CommandSender sender, CommandArguments args) throws InvalidArgumentException {
		
		String rentalName = args.getString("rentalName");
		int page = args.getInteger("page");

		RentRegionManager regionManager = RentalRooms.getInstance().getRegionManager();
		BillCollector billCollector = RentalRooms.getInstance().getBillCollector();
		
		RentRegion specifiedRegion = regionManager.getRegion(rentalName);
		
		if (specifiedRegion == null) {
			tellError(sender, Lang.get(_RENTAL_NOT_FOUND, rentalName));
			return; // finish
		}
		
		ChatPaginator pagin = Msg.getPaginator(Lang.get(_PAGINATOR_TITLE, specifiedRegion.getName()));
		
		String noneLabel = Lang.get(_LABEL_NONE);
		String vacantLabel = Lang.get(_LABEL_VACANT);
		String blocksLabel = Lang.get(_LABEL_BLOCKS);
		
		String label = null;
		
		label = Lang.get(_LABEL_TENANT);
		pagin.add(label, specifiedRegion.hasTenant() ? specifiedRegion.getTenant().getPlayerName() : vacantLabel);
		
		label = Lang.get(_LABEL_PRICE);
		pagin.add(label, billCollector.getFormattedRentAmount(specifiedRegion));
		
		label = Lang.get(_LABEL_VOLUME);
		pagin.add(label, specifiedRegion.getRentSpaceVolume() + " " + blocksLabel);
		
		
		if (sender instanceof Player) {
			
			Player p = (Player)sender;
			
			RentRegion standingRegion = regionManager.getRegion(p.getLocation());
			pagin.add(Lang.get(_STANDING_IN), standingRegion != null ? standingRegion.getName() : Lang.get(_NO_REGION_HERE));
			
			pagin.addFormatted(FormatTemplate.RAW, ChatColor.GREEN + Lang.get(_YOUR_RENTALS));
			
			Tenant tenant = Tenant.get(p);
			if (tenant == null) {
				pagin.addFormatted(FormatTemplate.RAW, noneLabel);			
			}
			else {
				List<RentRegion> regions = tenant.getRentRegions();
				if (regions.size() == 0) {
					pagin.addFormatted(FormatTemplate.RAW, noneLabel);
				}
				else {
					
				    String desc = Lang.get(_DESCRIPTION);
					for (RentRegion region : regions) {
						pagin.add(region.getName(), desc, billCollector.getFormattedRentAmount(region), billCollector.getRentCycle());
					}
				}
			}
		}
		
		
		
		pagin.show(sender, page, FormatTemplate.CONSTANT_DEFINITION);
	}
	
}

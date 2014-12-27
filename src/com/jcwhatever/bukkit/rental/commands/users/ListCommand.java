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

import com.jcwhatever.nucleus.commands.AbstractCommand;
import com.jcwhatever.nucleus.commands.CommandInfo;
import com.jcwhatever.nucleus.commands.arguments.CommandArguments;
import com.jcwhatever.nucleus.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.messaging.ChatPaginator;
import com.jcwhatever.nucleus.utils.text.TextUtils.FormatTemplate;
import com.jcwhatever.bukkit.rental.BillCollector;
import com.jcwhatever.bukkit.rental.Lang;
import com.jcwhatever.bukkit.rental.Msg;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.Tenant;
import com.jcwhatever.bukkit.rental.region.RentRegion;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

@CommandInfo(
		command="list", 
		staticParams={"page=1"},
		description="Get a list of your rented regions.",
		permissionDefault=PermissionDefault.TRUE)

public class ListCommand extends AbstractCommand {

	@Override
	public void execute(CommandSender sender, CommandArguments args) throws CommandException {

		CommandException.checkNotConsole(this, sender);

		Player p = (Player)sender;

		int page = args.getInteger("page");

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

		pagin.show(sender, page, FormatTemplate.CONSTANT_DEFINITION);
	}

}

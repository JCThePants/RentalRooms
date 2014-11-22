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


package com.jcwhatever.bukkit.rental.commands.admin;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.CommandInfo;
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

@CommandInfo(
		command="listall", 
		staticParams={"page=1"},
		usage="/rent listall [page]",
		description="List all rental regions.")

public class ListAllCommand extends AbstractCommand {
	
	@Override
	public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {
		
		int page = args.getInteger("page");
		
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

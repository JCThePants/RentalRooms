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


package com.jcwhatever.bukkit.rental.commands.users.friends;

import com.jcwhatever.generic.commands.AbstractCommand;
import com.jcwhatever.generic.commands.CommandInfo;
import com.jcwhatever.generic.commands.arguments.CommandArguments;
import com.jcwhatever.generic.commands.exceptions.CommandException;
import com.jcwhatever.generic.messaging.ChatPaginator;
import com.jcwhatever.generic.utils.text.TextUtils.FormatTemplate;
import com.jcwhatever.bukkit.rental.Friend;
import com.jcwhatever.bukkit.rental.Lang;
import com.jcwhatever.bukkit.rental.Msg;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.region.RentRegion;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

@CommandInfo(
		parent="friends",
		command="list", 
		staticParams={ "rentalName", "page=1"},
		description="List all friends in the specified rental.",
		permissionDefault=PermissionDefault.TRUE)

public class ListSubCommand extends AbstractCommand {
	
	@Override
	public void execute(CommandSender sender, CommandArguments args) throws CommandException {

		CommandException.assertNotConsole(this, sender);
				
		Player p = (Player)sender;
		
		String rentalName = args.getName("rentalName");
		int page = args.getInteger("page");
		
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
		
		pagin.show(sender, page, FormatTemplate.LIST_ITEM);
	}
}

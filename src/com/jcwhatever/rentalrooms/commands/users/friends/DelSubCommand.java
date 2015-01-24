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


package com.jcwhatever.rentalrooms.commands.users.friends;

import com.jcwhatever.rentalrooms.Lang;
import com.jcwhatever.rentalrooms.RentalRooms;
import com.jcwhatever.rentalrooms.region.RentRegion;
import com.jcwhatever.rentalrooms.region.RentRegionManager;
import com.jcwhatever.nucleus.commands.AbstractCommand;
import com.jcwhatever.nucleus.commands.CommandInfo;
import com.jcwhatever.nucleus.commands.arguments.CommandArguments;
import com.jcwhatever.nucleus.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.utils.language.Localizable;
import com.jcwhatever.nucleus.utils.player.PlayerUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.UUID;

@CommandInfo(
        parent="friends",
        command="del",
        staticParams={"rentalName", "playerName"},
        description="Remove a friend from the specified rented region.",
        paramDescriptions = {
                "rentalName= The name of the rental region.",
                "playerName= The name of the friend to remove from the rental region."
        },
        permissionDefault=PermissionDefault.TRUE)

public class DelSubCommand extends AbstractCommand {

    @Localizable static final String _REGION_NOT_FOUND =
            "Rental '{0: rental name}' was not found.";

    @Localizable static final String _NOT_TENANT =
            "You are not the tenant of rental '{0: rental name}'.";

    @Localizable static final String _PLAYER_NOT_FOUND =
            "'{0: player name}' was not found.";

    @Localizable static final String _SUCCESS =
            "'{0: player name}' has been removed as a friend from rental '{1: rental name}'.";


    @Override
    public void execute(CommandSender sender, CommandArguments args) throws CommandException{

        CommandException.checkNotConsole(this, sender);

        Player p = (Player)sender;

        String regionName = args.getName("rentalName");
        String playerName = args.getName("playerName");

        RentRegionManager regionManager = RentalRooms.getRegionManager();

        RentRegion region = regionManager.get(regionName);
        if (region == null) {
            tellError(p, Lang.get(_REGION_NOT_FOUND, regionName));
            return; // finish
        }

        if (!region.hasTenant() || !region.getTenant().equals(p)) {
            tellError(p, Lang.get(_NOT_TENANT, regionName));
            return; // finish
        }

        UUID friendId = PlayerUtils.getPlayerId(playerName);
        if (friendId == null) {
            tellError(p, Lang.get(_PLAYER_NOT_FOUND, playerName));
            return; // finish
        }

        region.getFriendManager().removeFriend(friendId);

        tellSuccess(p, Lang.get(_SUCCESS, playerName, regionName));
    }
}
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


package com.jcwhatever.rentalrooms.commands.users;

import com.jcwhatever.rentalrooms.Lang;
import com.jcwhatever.rentalrooms.RentalRooms;
import com.jcwhatever.rentalrooms.region.RentRegion;
import com.jcwhatever.rentalrooms.region.RentRegionManager;
import com.jcwhatever.nucleus.commands.AbstractCommand;
import com.jcwhatever.nucleus.commands.CommandInfo;
import com.jcwhatever.nucleus.commands.arguments.CommandArguments;
import com.jcwhatever.nucleus.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.language.Localizable;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

@CommandInfo(
        command="moveout",
        staticParams={"rentalName="},
        description="Move out of the rental unit you're standing in or specify.",
        paramDescriptions = {
                "rentalName= Optional. The name of the rent region to move out of. If not " +
                        "specified then the rent region you are standing in is used."
        },
        permissionDefault=PermissionDefault.TRUE)

public class MoveOutCommand extends AbstractCommand {

    @Localizable static final String _NOT_STANDING_IN_RENTAL =
            "You're not standing in a rental region. Stand in the region to move " +
                    "out of or specify the rental name.";

    @Localizable static final String _REGION_NOT_FOUND =
            "Rental region '{0: rental name}' not found.";

    @Localizable static final String _NOT_TENANT =
            "You can't move out because you aren't the tenant.";

    @Localizable static final String _SUCCESS =
            "You have moved out from '{0: rent name}'";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws CommandException {

        CommandException.checkNotConsole(this, sender);

        Player p = (Player)sender;

        RentRegionManager regionManager = RentalRooms.getRegionManager();


        RentRegion region;
        if (args.isDefaultValue("rentalName")) {
            region = regionManager.get(p.getLocation());
            if (region == null) {
                tellError(p, Lang.get(_NOT_STANDING_IN_RENTAL));
                return; // finish
            }
        } else {
            String rentalName = args.getString("rentalName");
            region = regionManager.get(rentalName);
            if (region == null) {
                tellError(p, Lang.get(_REGION_NOT_FOUND, rentalName));
                return; // finish
            }
        }

        if (!region.hasTenant() || !region.getTenant().equals(p)) {
            tellError(p, Lang.get(_NOT_TENANT));
            return; // finish
        }

        region.evict();

        tellSuccess(p, Lang.get(_SUCCESS, region.getName()));
    }
}
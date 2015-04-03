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


package com.jcwhatever.rentalrooms.commands.admin;

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


@CommandInfo(
        command="del",
        staticParams="rentalName",
        description="Remove a rental room.",
        paramDescriptions = {
                "rentalName= The name of the rental region. {NAME16}"
        })
public class DelCommand extends AbstractCommand {

    @Localizable static final String _REGION_NOT_FOUND =
            "A rental region named '{0: rental name}' was not found.";

    @Localizable static final String _FAILED =
            "Failed to remove rental region.";

    @Localizable static final String _SUCCESS =
            "Rental region '{0: region name}' removed.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws CommandException {

        String rentalName = args.getName("rentalName");

        RentRegionManager regionManager = RentalRooms.getRegionManager();

        RentRegion region = regionManager.get(rentalName);
        if (region == null) {
            tellError(sender, Lang.get(_REGION_NOT_FOUND, rentalName));
            return; // finish
        }

        if (!regionManager.remove(rentalName)) {
            tellError(sender, Lang.get(_FAILED));
            return; // finish
        }

        tellSuccess(sender, Lang.get(_SUCCESS, rentalName));
    }
}

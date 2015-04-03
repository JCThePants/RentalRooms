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

import com.jcwhatever.rentalrooms.BillCollector;
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
        command="pay",
        staticParams={"rentalName"},
        description="Pay the rent on the specified rental region.",
        paramDescriptions = {
                "rentalName= The name of the rent region to pay rent for."
        },
        permissionDefault=PermissionDefault.TRUE)

public class PayCommand extends AbstractCommand {

    @Localizable static final String _REGION_NOT_FOUND =
            "A rental region named '{0: rent name}' was not found.";

    @Localizable static final String _NOT_TENANT =
            "You aren't the tenant of rental region '{0: rent name}'.";

    @Localizable static final String _CANNOT_AFFORD =
            "You can't afford to pay {0: pay amount}. Please check your balance.";

    @Localizable static final String _CANNOT_PAY_ADVANCE =
            "You cannot pay more than {0: days} days in advance.";

    @Localizable static final String _PAY_FAILED =
            "Failed to pay for rental '{0: rent name}'.";

    @Localizable static final String _SUCCESS =
            "Payed {0: pay amount} for rental '{1: rent name}'. Next payment due {2: due date}.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws CommandException {

        CommandException.checkNotConsole(this, sender);

        Player p = (Player)sender;

        String rentalName = args.getName("rentalName");

        RentRegionManager regionManager = RentalRooms.getRegionManager();

        RentRegion region = regionManager.get(rentalName);
        if (region == null) {
            tellError(p, Lang.get(_REGION_NOT_FOUND, rentalName));
            return; // finish
        }

        if (!region.hasTenant() || !region.getTenant().getPlayerID().equals(p.getUniqueId())) {
            tellError(p, Lang.get(_NOT_TENANT, region.getName()));
            return; // finish
        }

        BillCollector collector = RentalRooms.getBillCollector();

        if (!collector.canPay(region, p)) {
            tellError(p, Lang.get(_CANNOT_AFFORD, collector.formatRentPrice(region)));
            return; // finish
        }

        if (!collector.canPayAdvance(region)) {
            tellError(p, Lang.get(_CANNOT_PAY_ADVANCE, collector.getMaxAdvancedDays()));
            return; // finish
        }

        if (!collector.charge(region, p)) {
            tellError(p, Lang.get(_PAY_FAILED, region.getName()));
            return; // finish
        }

        String amountPayed = collector.formatRentPrice(region);
        String nextDueDate = region.getFormattedExpiration();

        tellSuccess(p, Lang.get(_SUCCESS, amountPayed, region.getName(), nextDueDate));
    }
}


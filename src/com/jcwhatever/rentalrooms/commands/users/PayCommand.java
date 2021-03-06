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

import com.jcwhatever.nucleus.managed.commands.CommandInfo;
import com.jcwhatever.nucleus.managed.commands.arguments.ICommandArguments;
import com.jcwhatever.nucleus.managed.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.commands.mixins.IExecutableCommand;
import com.jcwhatever.nucleus.managed.commands.utils.AbstractCommand;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.rentalrooms.BillCollector;
import com.jcwhatever.rentalrooms.Lang;
import com.jcwhatever.rentalrooms.RentalRooms;
import com.jcwhatever.rentalrooms.region.RentRegion;
import com.jcwhatever.rentalrooms.region.RentRegionManager;

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

public class PayCommand extends AbstractCommand implements IExecutableCommand {

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
    public void execute(CommandSender sender, ICommandArguments args) throws CommandException {

        CommandException.checkNotConsole(getPlugin(), this, sender);

        Player p = (Player)sender;

        String rentalName = args.getName("rentalName");

        RentRegionManager regionManager = RentalRooms.getRegionManager();

        RentRegion region = regionManager.get(rentalName);
        if (region == null)
            throw new CommandException(Lang.get(_REGION_NOT_FOUND, rentalName));

        if (!region.hasTenant() || !region.getTenant().getPlayerID().equals(p.getUniqueId()))
            throw new CommandException(Lang.get(_NOT_TENANT, region.getName()));

        BillCollector collector = RentalRooms.getBillCollector();

        if (!collector.canPay(region, p))
            throw new CommandException(Lang.get(_CANNOT_AFFORD, collector.formatRentPrice(region)));

        if (!collector.canPayAdvance(region))
            throw new CommandException(Lang.get(_CANNOT_PAY_ADVANCE, collector.getMaxAdvancedDays()));

        if (!collector.charge(region, p))
            throw new CommandException(Lang.get(_PAY_FAILED, region.getName()));

        String amountPayed = collector.formatRentPrice(region);
        String nextDueDate = region.getFormattedExpiration();

        tellSuccess(p, Lang.get(_SUCCESS, amountPayed, region.getName(), nextDueDate));
    }
}


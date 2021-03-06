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
        command="movein",
        description="Move in to the rental unit you're standing in.",
        permissionDefault=PermissionDefault.TRUE)

public class MoveInCommand extends AbstractCommand implements IExecutableCommand {

    @Localizable static final String _NOT_STANDING_IN_RENTAL =
            "You're not standing in a rental unit. Stand in the unit you want to rent " +
                    "before running the movein command.";

    @Localizable static final String _ALREADY_RENTING =
            "You're already renting this unit.";

    @Localizable static final String _ALREADY_RENTED =
            "You can't rent this unit because it already has a tenant.";

    @Localizable static final String _CANNOT_AFFORD =
            "You can't afford to rent this unit.";

    @Localizable static final String _PAY_FAILED =
            "Failed to pay for rental '{0: rental name}'.";

    @Localizable static final String _SUCCESS =
            "You are now the tenant of this rental. {0: pay amount} has been taken from your account " +
                    "and you will be expected to pay the rent again in {1: days} real life days.";

    @Override
    public void execute(CommandSender sender, ICommandArguments args) throws CommandException {

        CommandException.checkNotConsole(getPlugin(), this, sender);

        Player p = (Player)sender;

        RentRegionManager regionManager = RentalRooms.getRegionManager();

        RentRegion region = regionManager.get(p.getLocation());
        if (region == null)
            throw new CommandException(Lang.get(_NOT_STANDING_IN_RENTAL));

        if (region.hasTenant()) {

            if (region.getTenant().equals(p))
                throw new CommandException(Lang.get(_ALREADY_RENTING));

            throw new CommandException(Lang.get(_ALREADY_RENTED));
        }

        BillCollector collector = RentalRooms.getBillCollector();

        if (!collector.canPay(region, p))
            throw new CommandException(Lang.get(_CANNOT_AFFORD));

        if (!collector.charge(region, p))
            throw new CommandException(Lang.get(_PAY_FAILED, region.getName()));

        region.setTenant(p);

        tellSuccess(p, Lang.get(_SUCCESS,
                collector.formatRentPrice(region),
                collector.getRentCycle()));
    }
}


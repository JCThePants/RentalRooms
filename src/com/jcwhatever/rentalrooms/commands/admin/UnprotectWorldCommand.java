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
import com.jcwhatever.rentalrooms.region.RentRegionManager;
import com.jcwhatever.nucleus.commands.AbstractCommand;
import com.jcwhatever.nucleus.commands.CommandInfo;
import com.jcwhatever.nucleus.commands.arguments.CommandArguments;
import com.jcwhatever.nucleus.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.language.Localizable;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(
        command="unprotectworld",
        staticParams={"worldName="},
        description="Remove world from protection.")

public class UnprotectWorldCommand extends AbstractCommand {

    @Localizable static final String _WORLD_NOT_FOUND =
            "A world with the name '{0: world name}' was not found.";

    @Localizable static final String _FAILED =
            "Failed to remove world '{0: world name}'.";

    @Localizable static final String _SUCCESS =
            "World '{0: world name}' is no longer a protected world.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws CommandException {

        String worldName;

        if (args.isDefaultValue("worldName")) {

            CommandException.checkNotConsole(this, sender);

            worldName = ((Player)sender).getWorld().getName();
        }
        else {
            worldName = args.getName("worldName");
        }

        RentRegionManager regionManager = RentalRooms.getRegionManager();

        boolean isRemoved = regionManager.removeProtectedWorld(worldName);
        if (!isRemoved) {
            World world = Bukkit.getWorld(worldName);

            if (world == null) {
                tellError(sender, Lang.get(_WORLD_NOT_FOUND, worldName));
                return; // finish
            }
            else {
                isRemoved = regionManager.removeProtectedWorld(world.getName());
            }
        }

        if (!isRemoved) {
            tellError(sender, Lang.get(_FAILED, worldName));
            return; // finish
        }

        tellSuccess(sender, Lang.get(_SUCCESS, worldName));
    }
}

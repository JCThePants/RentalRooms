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

import com.jcwhatever.nucleus.managed.commands.CommandInfo;
import com.jcwhatever.nucleus.managed.commands.arguments.ICommandArguments;
import com.jcwhatever.nucleus.managed.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.commands.mixins.IExecutableCommand;
import com.jcwhatever.nucleus.managed.commands.utils.AbstractCommand;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.rentalrooms.Lang;
import com.jcwhatever.rentalrooms.RentalRooms;
import com.jcwhatever.rentalrooms.region.RentRegionManager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(
        command="protectworld",
        staticParams={"worldName="},
        description="Prevent editing entire world you are in or specify a world.",
        paramDescriptions = {
                "worldName= The name of the world to protect."
        })
public class ProtectWorldCommand extends AbstractCommand implements IExecutableCommand {

    @Localizable static final String _WORLD_NOT_FOUND =
            "A world with the name '{0: world name}' was not found.";

    @Localizable static final String _FAILED =
            "Failed to add world '{0: world name}'.";

    @Localizable static final String _SUCCESS =
            "World '{0: world name}' is now a protected world.";

    @Override
    public void execute(CommandSender sender, ICommandArguments args) throws CommandException {

        String worldName ;

        if (args.isDefaultValue("worldName")) {

            CommandException.checkNotConsole(getPlugin(), this, sender);

            worldName = ((Player)sender).getWorld().getName();
        }
        else {
            worldName = args.getString("worldName");
        }

        RentRegionManager regionManager = RentalRooms.getRegionManager();
        World world = Bukkit.getWorld(worldName);

        if (world == null)
            throw new CommandException(Lang.get(_WORLD_NOT_FOUND, worldName));

        if (!regionManager.addProtectedWorld(world))
            throw new CommandException(Lang.get(_FAILED, world.getName()));

        tellSuccess(sender, Lang.get(_SUCCESS, world.getName()));
    }
}

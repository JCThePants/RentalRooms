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

import com.jcwhatever.nucleus.commands.AbstractCommand;
import com.jcwhatever.nucleus.commands.CommandInfo;
import com.jcwhatever.nucleus.commands.arguments.CommandArguments;
import com.jcwhatever.nucleus.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.regions.selection.IRegionSelection;
import com.jcwhatever.bukkit.rental.RentalRooms;
import com.jcwhatever.bukkit.rental.region.RentRegion;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


@CommandInfo(
        command="setregion", 
        staticParams={"rentalName"},
        description="Set the a rental rooms region using your current region selection.")

public class SetRegionCommand extends AbstractCommand {
    
    @Override
    public void execute(CommandSender sender, CommandArguments args) throws CommandException {

        CommandException.checkNotConsole(this, sender);

        String rentalName = args.getName("rentalName");
        
        Player p = (Player)sender;
        
        IRegionSelection selection = getRegionSelection(p);
        if (selection == null)
            return; // finish
        
        Location p1 = selection.getP1();
        Location p2 = selection.getP2();

        RentRegionManager regionManager = RentalRooms.getInstance().getRegionManager();
        
        RentRegion region = regionManager.getRegion(rentalName);
        if (region == null) {
            tellError(p, "A rental with the name '{0}' was not found.", rentalName);
            return; // finish
        }
        
        region.setCoords(p1, p2);
        region.clearInterior();
        
        tellSuccess(p, "Rental region '{0}' set. Interior cleared.", rentalName);
    }
    

}
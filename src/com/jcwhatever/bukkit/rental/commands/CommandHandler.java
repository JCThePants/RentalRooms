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


package com.jcwhatever.bukkit.rental.commands;

import org.bukkit.plugin.Plugin;

import com.jcwhatever.bukkit.generic.commands.AbstractCommandHandler;
import com.jcwhatever.bukkit.rental.commands.admin.CreateCommand;
import com.jcwhatever.bukkit.rental.commands.admin.DelCommand;
import com.jcwhatever.bukkit.rental.commands.admin.EditModeCommand;
import com.jcwhatever.bukkit.rental.commands.admin.ListAllCommand;
import com.jcwhatever.bukkit.rental.commands.admin.ProtectWorldCommand;
import com.jcwhatever.bukkit.rental.commands.admin.SetMaxAdvanceCommand;
import com.jcwhatever.bukkit.rental.commands.admin.SetPriceCommand;
import com.jcwhatever.bukkit.rental.commands.admin.SetRegionCommand;
import com.jcwhatever.bukkit.rental.commands.admin.SetRentCycleCommand;
import com.jcwhatever.bukkit.rental.commands.admin.UnprotectWorldCommand;
import com.jcwhatever.bukkit.rental.commands.admin.interior.InteriorCommand;
import com.jcwhatever.bukkit.rental.commands.users.InfoCommand;
import com.jcwhatever.bukkit.rental.commands.users.ListCommand;
import com.jcwhatever.bukkit.rental.commands.users.MoveInCommand;
import com.jcwhatever.bukkit.rental.commands.users.MoveOutCommand;
import com.jcwhatever.bukkit.rental.commands.users.PayCommand;
import com.jcwhatever.bukkit.rental.commands.users.friends.FriendsCommand;

public class CommandHandler extends AbstractCommandHandler {

	public CommandHandler(Plugin plugin) {
		super(plugin);
	}

	@Override
	protected void registerCommands() {
		
		this.registerCommand(CreateCommand.class);
		this.registerCommand(InteriorCommand.class);
		
		this.registerCommand(InfoCommand.class);
		this.registerCommand(MoveInCommand.class);
		this.registerCommand(MoveOutCommand.class);
		this.registerCommand(EditModeCommand.class);
		this.registerCommand(ListAllCommand.class);
		this.registerCommand(ProtectWorldCommand.class);
		this.registerCommand(UnprotectWorldCommand.class);
		this.registerCommand(SetPriceCommand.class);
		this.registerCommand(SetRentCycleCommand.class);
		this.registerCommand(ListCommand.class);
		this.registerCommand(PayCommand.class);
		this.registerCommand(FriendsCommand.class);
		this.registerCommand(DelCommand.class);
		this.registerCommand(SetRegionCommand.class);
		this.registerCommand(SetMaxAdvanceCommand.class);
	}

}

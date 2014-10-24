package com.jcwhatever.bukkit.rental.commands.admin;

import org.bukkit.command.CommandSender;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.rental.BillCollector;
import com.jcwhatever.bukkit.rental.RentalRooms;

@ICommandInfo(
		command="setrentcycle", 
		staticParams={"days"},
		usage="/rent setrentcycle <days>",
		description="Set rent cycle in real life days.")

public class SetRentCycleCommand extends AbstractCommand {
	
	@Override
	public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {
		
		int days = args.getInt("days");
		
		BillCollector billCollector = RentalRooms.getInstance().getBillCollector();
		
		billCollector.setRentCycle(days);
		
		tellSuccess(sender, "Rent cycle changed to {0} real life days.", days);
	}
}

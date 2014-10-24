package com.jcwhatever.bukkit.rental.commands.admin;

import com.jcwhatever.bukkit.generic.economy.EconomyHelper;
import org.bukkit.command.CommandSender;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.rental.BillCollector;
import com.jcwhatever.bukkit.rental.RentalRooms;

@ICommandInfo(
		command="setprice", 
		staticParams={"price"},
		usage="/rent setprice <price>",
		description="Set price per unit of volume of rental.")

public class SetPriceCommand extends AbstractCommand {
	
	@Override
	public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {
		
		double price = args.getDouble("price");
		
		BillCollector billCollector = RentalRooms.getInstance().getBillCollector();
		
		billCollector.setCostPerVolume(price);
		
		tellSuccess(sender, "Cost per volume set to {0}", EconomyHelper.formatAmount(price));
	}
}

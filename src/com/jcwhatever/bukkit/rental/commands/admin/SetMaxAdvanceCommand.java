package com.jcwhatever.bukkit.rental.commands.admin;

import org.bukkit.command.CommandSender;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.rental.BillCollector;
import com.jcwhatever.bukkit.rental.RentalRooms;


@ICommandInfo(
        command="setmaxadvance", 
        staticParams={"days"},
        usage="/rent setmaxadvance <days>",
        description="Set max days players can pay rent in advance.")

public class SetMaxAdvanceCommand extends AbstractCommand {
    
    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {
        
        int days = args.getInt("days");
        
        if (days < 0) {
            tellError(sender, "<days> must be a value higher or equal to 0.");
            return; // finish
        }
        
        BillCollector billCollector = RentalRooms.getInstance().getBillCollector();
        
        billCollector.setMaxAdvanceDays(days);
        
        tellSuccess(sender, "Max advance days set to {0}", days);
    }
}

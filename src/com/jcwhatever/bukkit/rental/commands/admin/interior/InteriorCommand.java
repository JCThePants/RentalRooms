package com.jcwhatever.bukkit.rental.commands.admin.interior;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;

@ICommandInfo(
		command="interior", 
		description="Interior options.")

public class InteriorCommand extends AbstractCommand {
	
    public InteriorCommand() {
        super();
        
        registerSubCommand(AddSubCommand.class);
        registerSubCommand(ClearSubCommand.class);
    }
	
}

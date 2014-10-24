package com.jcwhatever.bukkit.rental.commands.users.friends;

import org.bukkit.permissions.PermissionDefault;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;

@ICommandInfo(
		command="friends", 
		description="Manage friends.",
		permissionDefault=PermissionDefault.TRUE)

public class FriendsCommand extends AbstractCommand {
	
	public FriendsCommand() {
		super();
		
		this.registerSubCommand(AddSubCommand.class);
		this.registerSubCommand(DelSubCommand.class);
		this.registerSubCommand(ListSubCommand.class);
	}
}


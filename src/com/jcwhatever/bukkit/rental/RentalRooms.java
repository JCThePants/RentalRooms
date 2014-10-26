package com.jcwhatever.bukkit.rental;

import com.jcwhatever.bukkit.generic.GenericsPlugin;
import com.jcwhatever.bukkit.generic.signs.SignManager;
import com.jcwhatever.bukkit.rental.commands.CommandHandler;
import com.jcwhatever.bukkit.rental.events.GlobalListener;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;
import com.jcwhatever.bukkit.rental.signs.RentalSignHandler;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;

public class RentalRooms extends GenericsPlugin {

	private static RentalRooms _instance;
	private RentRegionManager _region;
	private GlobalListener _listener;
	private BillCollector _billCollector;
    private SignManager _signManager;
    private RentalSignHandler _signHandler;
	
	public static final String DATE_FORMAT = "MMM d, yyyy 'at' hh:mm aaa zzzz";
	
	public RentalRooms() {
		_instance = this;
	}
	
	public static RentalRooms getInstance() {
		return _instance;
	}
	
	public RentRegionManager getRegionManager() {
		return _region;
	}
	
	public BillCollector getBillCollector() {
		return _billCollector;
	}

    public SignManager getSignManager() {
        return _signManager;
    }

    public RentalSignHandler getSignHandler() {
        return _signHandler;
    }
	
	@Override
	public String getChatPrefix() {
		return ChatColor.GOLD + "[Rental] " + ChatColor.RESET;
	}

	@Override
	public String getConsolePrefix() {
		return "[Rental] ";
	}

    @Override
    protected void onEnablePlugin() {
        _region = new RentRegionManager(getDataNode().getNode("regions"));
        _listener = new GlobalListener(_region);
        _signManager = new SignManager(this, getDataNode().getNode("signs.rental"));
        _billCollector = new BillCollector(this.getDataNode().getNode("econ"));


        _signHandler = new RentalSignHandler();
        _signManager.registerSignType(_signHandler);

        registerListeners();
    }

    @Override
    protected void onDisablePlugin() {

    }

	private void registerListeners() {
        CommandHandler handler = new CommandHandler(this);
        getCommand("rent").setExecutor(handler);
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(_listener, this);
    }
	
	

}

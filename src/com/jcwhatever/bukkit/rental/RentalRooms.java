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


package com.jcwhatever.bukkit.rental;

import com.jcwhatever.nucleus.NucleusPlugin;
import com.jcwhatever.nucleus.signs.SignManager;
import com.jcwhatever.bukkit.rental.commands.RentalCommandDispatcher;
import com.jcwhatever.bukkit.rental.events.GlobalListener;
import com.jcwhatever.bukkit.rental.region.RentRegionManager;
import com.jcwhatever.bukkit.rental.signs.RentalSignHandler;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;

public class RentalRooms extends NucleusPlugin {

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
        RentalCommandDispatcher handler = new RentalCommandDispatcher(this);
        getCommand("rent").setExecutor(handler);
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(_listener, this);
    }
	
	

}

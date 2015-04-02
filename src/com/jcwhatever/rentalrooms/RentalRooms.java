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


package com.jcwhatever.rentalrooms;

import com.jcwhatever.nucleus.NucleusPlugin;
import com.jcwhatever.nucleus.utils.DateUtils;
import com.jcwhatever.nucleus.utils.language.Localizable;
import com.jcwhatever.nucleus.utils.signs.SignUtils;
import com.jcwhatever.rentalrooms.commands.RentalCommandDispatcher;
import com.jcwhatever.rentalrooms.events.GlobalListener;
import com.jcwhatever.rentalrooms.region.RentRegionManager;

import org.bukkit.ChatColor;

import java.util.Date;

/**
 * RentalRooms plugin.
 */
public class RentalRooms extends NucleusPlugin {

    @Localizable
    static final String _DATE_FORMAT = "MMM d, yyyy 'at' hh:mm aaa zzzz";

    private static RentalRooms _instance;
    private RentRegionManager _regionManager;
    private BillCollector _billCollector;
    private RentalSignHandler _signHandler;

    /**
     * Get the plugin instance.
     */
    public static RentalRooms getPlugin() {
        return _instance;
    }

    /**
     * Get the date format string to use.
     */
    public static String formatDate(Date date) {
        return DateUtils.format(date, Lang.get(_DATE_FORMAT));
    }

    /**
     * Get the rent region manager.
     */
    public static RentRegionManager getRegionManager() {
        return _instance._regionManager;
    }

    /**
     * Get the bill collector.
     */
    public static BillCollector getBillCollector() {
        return _instance._billCollector;
    }

    /**
     * Get the sign handler.
     */
    public static RentalSignHandler getSignHandler() {
        return _instance._signHandler;
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
    protected void onInit() {
        _instance = this;
    }

    @Override
    protected void onEnablePlugin() {

        _instance = this;

        _regionManager = new RentRegionManager(getDataNode().getNode("regions"));
        _billCollector = new BillCollector(this.getDataNode().getNode("econ"));

        _signHandler = new RentalSignHandler();
        SignUtils.registerHandler(_signHandler);

        registerEventListeners(new GlobalListener(_regionManager));
        registerCommands(new RentalCommandDispatcher(this));
    }

    @Override
    protected void onDisablePlugin() {
        _instance = null;

        SignUtils.unregisterHandler(_signHandler);
    }
}

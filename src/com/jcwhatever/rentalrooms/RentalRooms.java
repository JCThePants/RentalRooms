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
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.nucleus.utils.DateUtils;
import com.jcwhatever.nucleus.utils.SignUtils;
import com.jcwhatever.rentalrooms.commands.admin.CreateCommand;
import com.jcwhatever.rentalrooms.commands.admin.DelCommand;
import com.jcwhatever.rentalrooms.commands.admin.EditModeCommand;
import com.jcwhatever.rentalrooms.commands.admin.ListAllCommand;
import com.jcwhatever.rentalrooms.commands.admin.ProtectWorldCommand;
import com.jcwhatever.rentalrooms.commands.admin.SetMaxAdvanceCommand;
import com.jcwhatever.rentalrooms.commands.admin.SetPriceCommand;
import com.jcwhatever.rentalrooms.commands.admin.SetRegionCommand;
import com.jcwhatever.rentalrooms.commands.admin.SetRentCycleCommand;
import com.jcwhatever.rentalrooms.commands.admin.UnprotectWorldCommand;
import com.jcwhatever.rentalrooms.commands.admin.interior.InteriorCommand;
import com.jcwhatever.rentalrooms.commands.users.InfoCommand;
import com.jcwhatever.rentalrooms.commands.users.ListCommand;
import com.jcwhatever.rentalrooms.commands.users.MoveInCommand;
import com.jcwhatever.rentalrooms.commands.users.MoveOutCommand;
import com.jcwhatever.rentalrooms.commands.users.PayCommand;
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
        this.registerCommand(DelCommand.class);
        this.registerCommand(SetRegionCommand.class);
        this.registerCommand(SetMaxAdvanceCommand.class);

        registerEventListeners(new GlobalListener(_regionManager));
    }

    @Override
    protected void onDisablePlugin() {
        _instance = null;

        SignUtils.unregisterHandler(_signHandler);
    }
}

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

import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.nucleus.managed.signs.ISignContainer;
import com.jcwhatever.nucleus.managed.signs.SignHandler;
import com.jcwhatever.nucleus.providers.economy.Economy;
import com.jcwhatever.nucleus.utils.DateUtils;
import com.jcwhatever.nucleus.utils.DateUtils.TimeRound;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.player.PlayerUtils;
import com.jcwhatever.rentalrooms.events.RentMoveInEvent;
import com.jcwhatever.rentalrooms.events.RentMoveOutEvent;
import com.jcwhatever.rentalrooms.events.RentPayedEvent;
import com.jcwhatever.rentalrooms.events.RentPriceChangedEvent;
import com.jcwhatever.rentalrooms.region.RentRegion;
import com.jcwhatever.rentalrooms.region.RentRegionManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * Rental sign handler.
 */
public class RentalSignHandler extends SignHandler {

    @Localizable static final String _SIGN_HEADER = "Rental";

    @Localizable static final String _DESCRIPTION =
            "An information display sign for a rental room. <regionName> is the name of the rental room region.";

    @Localizable static final String _MOVE_IN_INSTRUCTIONS =
            "{WHITE}Type {YELLOW}'/rent movein'{WHITE} while standing in the rent area to move in.";

    @Localizable static final String _INFO_FOR_TENANT =
            "{WHITE}Type {YELLOW}'/rent pay {0: regionName}'{WHITE} to pay your rent in the amount " +
                    "of {1: amount due}. Next payment due by {2: due date}.";

    @Localizable static final String _INFO_FOR_STRANGER =
            "Currently being rented by {0: tenant name}.";

    @Localizable static final String _SIGN_EXPIRING_SOON =
            "{RED}Expiring soon";

    @Localizable static final String _SIGN_HOURS_LEFT =
            "{0: hours} hours";

    @Localizable static final String _SIGN_DAYS_LEFT =
            "{0: days} days {1: hours} hrs";

    @Localizable static final String _SIGN_TENANT_NAME =
            "{DARK_BLUE}{0: tenant name}";

    @Localizable static final String _SIGN_FOR_RENT =
            "{GREEN}For Rent";

    private final RentRegionManager _regionManager;
    private final Collection<ISignContainer> _signContainerOutput = new ArrayList<>(10);

    /**
     * Constructor.
     */
    public RentalSignHandler () {
        super(RentalRooms.getPlugin(), Lang.get(_SIGN_HEADER));
        _regionManager = RentalRooms.getRegionManager();

        Bukkit.getPluginManager().registerEvents(new EventListener(), RentalRooms.getPlugin());
    }

    @Override
    public String getDescription() {
        return Lang.get(_DESCRIPTION).toString();
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                "Rental",
                "<regionName>",
                "--reserved--",
                "--reserved--"
        };
    }

    @Override
    public String getHeaderPrefix() {
        return ChatColor.DARK_BLUE.toString();
    }

    @Override
    protected SignClickResult onSignClick(Player player, ISignContainer sign) {

        String regionName = sign.getRawLine(1);

        // get the region indicated on the sign (line 2, index 1)
        RentRegion region = _regionManager.get(regionName);
        if (region == null)
            return SignClickResult.IGNORED;

        // check for tenant, show move in instructions if none.
        if (!region.hasTenant()) {
            Msg.tell(player, Lang.get(_MOVE_IN_INSTRUCTIONS));
            return SignClickResult.IGNORED;
        }

        Tenant tenant = region.getTenant();

        if (player.getUniqueId().equals(tenant.getPlayerID())) {

            // show tenant info about rent region

            BillCollector billCollector = RentalRooms.getBillCollector();

            Msg.tell(player, Lang.get(_INFO_FOR_TENANT,
                    region.getName(),
                    billCollector.formatRentPrice(region),
                    region.getFormattedExpiration()));
        }
        else {
            // show stranger info about rent region
            Msg.tell(player, Lang.get(_INFO_FOR_STRANGER, tenant.getPlayerName()));
        }

        return SignClickResult.HANDLED;
    }

    @Override
    public SignChangeResult onSignChange(Player player, ISignContainer sign) {
        if (!player.isOp())
            return SignChangeResult.INVALID;

        String regionName = sign.getRawLine(1);

        // get the region indicated on the sign (line 2, index 1)
        RentRegion region = _regionManager.get(regionName);
        if (region == null)
            return SignChangeResult.INVALID;

        setSignInfoForRegion(region, sign);

        return SignChangeResult.VALID;
    }

    @Override
    public SignBreakResult onSignBreak(Player player, ISignContainer sign) {
        String regionName = sign.getRawLine(1);

        RentRegion region = _regionManager.get(regionName);

        return region == null || player.isOp() && region.isEditModeOn()
                ? SignBreakResult.ALLOW
                : SignBreakResult.DENY;
    }

    @Override
    public void onSignLoad(ISignContainer sign) {
        // do nothing
    }

    // update the time left indicated on all signs for the specified region
    public void updateTimeLeft(RentRegion region) {
        PreCon.notNull(region);

        Date expires = region.getExpirationDate();
        if (expires == null)
            expires = new Date();

        long hoursLeft = DateUtils.getDeltaHours(new Date(), expires, TimeRound.ROUND_DOWN);
        long daysLeft = hoursLeft / 24;

        _signContainerOutput.clear();
        Collection<ISignContainer> signs = Nucleus.getSignManager().getSigns(getName(), _signContainerOutput);

        for (ISignContainer sign : signs) {
            if (isRegionSign(region, sign.getSign())) {

                if (hoursLeft <= 1) {
                    sign.setLine(3, Lang.get(_SIGN_EXPIRING_SOON));
                }
                else {
                    sign.setLine(3, hoursLeft <= 24
                            ? Lang.get(_SIGN_HOURS_LEFT, hoursLeft)
                            : Lang.get(_SIGN_DAYS_LEFT, daysLeft, hoursLeft % 24));
                }
                sign.update();
            }
        }

        _signContainerOutput.clear();
    }

    // determine if a sign is assigned to the specified region.
    private boolean isRegionSign(RentRegion region, Sign sign) {
        String regionName = sign.getLine(1);
        return _regionManager.get(regionName) == region;
    }

    // update the region owner indicated on all signs for the specified region.
    private void updateSignOwners(RentRegion region) {

        _signContainerOutput.clear();
        Collection<ISignContainer> signs = Nucleus.getSignManager().getSigns(getName(), _signContainerOutput);

        for (ISignContainer sign : signs) {
            if (region == null || isRegionSign(region, sign.getSign())) {

                RentRegion signRegion = region != null
                        ? region
                        : _regionManager.get(sign.getLine(1));

                if (signRegion == null)
                    continue;

                setSignInfoForRegion(signRegion, sign);

                sign.update();
            }
        }

        _signContainerOutput.clear();
    }

    // update price indicated on all rent region signs
    private void updateSignPrices(double newAmount) {

        _signContainerOutput.clear();
        Collection<ISignContainer> signs = Nucleus.getSignManager().getSigns(getName(), _signContainerOutput);

        for (ISignContainer sign : signs) {

            RentRegion signRegion = _regionManager.get(sign.getLine(1));
            if (signRegion == null)
                continue;

            String formatted = "";

            if (!signRegion.hasTenant()) {
                double newRegionAmount = newAmount * signRegion.getInteriorVolume();
                formatted = Economy.getCurrency().format(newRegionAmount);
            }

            sign.setLine(3, formatted);
            sign.update();
        }

        _signContainerOutput.clear();
    }

    // update the info on a sign to reflect the info on the specified rent region.
    private void setSignInfoForRegion(RentRegion region, ISignContainer sign) {
        PreCon.notNull(region);

        BillCollector billCollector = RentalRooms.getBillCollector();

        Player tenant = region.hasTenant()
                ? PlayerUtils.getPlayer(region.getTenant().getPlayerID())
                : null;

        String tenantName = region.hasTenant()
                ? tenant != null
                    ? tenant.getName()
                    : "[unknown]"
                : null;

        sign.setLine(2, tenantName != null
                ? Lang.get(_SIGN_TENANT_NAME, tenantName)
                : Lang.get(_SIGN_FOR_RENT));

        sign.setLine(3, region.hasTenant()
                ? ""
                :  billCollector.formatRentPrice(region));
    }

    /**
     * Listen for rent events so sign can be updated
     */
    private class EventListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
        private void onRentMoveIn(RentMoveInEvent event) {
            updateSignOwners(event.getRentRegion());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
        private void onRentMoveOut(RentMoveOutEvent event) {
            updateSignOwners(event.getRentRegion());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
        private void onPriceChanged(RentPriceChangedEvent event) {
            updateSignPrices(event.getNewPrice());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
        private void onRentPayed(RentPayedEvent event) {
            updateTimeLeft(event.getRentRegion());
        }
    }
}

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

import com.jcwhatever.rentalrooms.events.RentPayedEvent;
import com.jcwhatever.rentalrooms.events.RentPriceChangedEvent;
import com.jcwhatever.rentalrooms.region.RentRegion;
import com.jcwhatever.rentalrooms.region.RentRegionManager;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.DateUtils;
import com.jcwhatever.nucleus.utils.DateUtils.TimeRound;
import com.jcwhatever.nucleus.providers.economy.Economy;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.Rand;
import com.jcwhatever.nucleus.managed.scheduler.Scheduler;
import com.jcwhatever.nucleus.utils.language.Localizable;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Date;

/**
 * Responsible for tracking and collecting rent from tenants.
 */
public class BillCollector {

    @Localizable static final String _RENT_DUE_MINUTES =
            "{0: rent amount} Rent will be due for '{1: rent name}' in {2: minutes} minutes.";

    @Localizable static final String _RENT_DUE_HOURS =
            "{0: rent amount} Rent will be due for '{1: rent name}' in {2: hours} hours.";

    private double _costPerVolume = 1.0D;
    private int _rentCycle = 1; // term units are real life days
    private int _maxAdvanceDays = 3;

    private IDataNode _dataNode;

    /**
     * Constructor.
     *
     * @param dataNode  The collectors data node.
     */
    public BillCollector(IDataNode dataNode) {

        _dataNode = dataNode;

        load();

        // check dues every 1 minute.
        Scheduler.runTaskRepeatAsync(RentalRooms.getPlugin(), Rand.getInt(1, 600), 1200, new DueChecker());
    }

    /**
     * Get the maximum number of real life days a tenant is allowed to pay in advance.
     *
     * <p>The default is 3.</p>
     */
    public int getMaxAdvancedDays() {
        return _maxAdvanceDays;
    }

    /**
     * Set the maximum number of real life days a tenant is allowed to pay in advance.
     *
     * @param maxAdvance  The max number of days.
     */
    public void setMaxAdvanceDays(int maxAdvance) {
        _maxAdvanceDays = maxAdvance;

        _dataNode.set("max-advance-days", maxAdvance);
        _dataNode.save();
    }

    /**
     * Get the cost per unit of volume.
     *
     * <p>The unit of volume is a block and the counted blocks are the
     * blocks that the tenant is allowed to modify. Therefore the more
     * blocks the tenant can modify, the more the rent costs.</p>
     */
    public double getCostPerVolume() {
        return _costPerVolume;
    }

    /**
     * Set the cost per unit of volume.
     *
     * <p>The unit of volume is a block and the counted blocks are the
     * blocks that the tenant is allowed to modify. Therefore the more
     * blocks the tenant can modify, the more the rent costs.</p>
     *
     * @param costPerVolume  The price of one block.
     */
    public void setCostPerVolume(double costPerVolume) {

        RentPriceChangedEvent event = RentPriceChangedEvent.call(this, _costPerVolume, costPerVolume);
        if (event.isCancelled())
            return;

        _costPerVolume = event.getNewPrice();
        _dataNode.set("cost-per-volume", event.getNewPrice());
        _dataNode.save();
    }

    /**
     * Get the number of real life days in a rent cycle.
     *
     * <p>The rent cycle is the number of real life days that a single
     * rent payment is good for.</p>
     */
    public int getRentCycle() {
        return Math.max(1, _rentCycle);
    }

    /**
     * Set the number of real life days in a rent cycle.
     *
     * <p>The rent cycle is the number of real life days that a single
     * rent payment is good for.</p>
     *
     * @param rentCycle  The number of days in a cycle.
     */
    public void setRentCycle(int rentCycle) {
        _rentCycle = rentCycle;
        _dataNode.set("rent-cycle", rentCycle);
        _dataNode.save();
    }

    /**
     * Get the cost of renting the specified region.
     *
     * @param region  The region to check.
     */
    public double getRentCost(RentRegion region) {
        PreCon.notNull(region);

        return region.getInteriorVolume() * _costPerVolume;
    }

    /**
     * Determine if a player has the necessary economy balance to pay
     * the rent on a specified rent region.
     *
     * @param region  The region to check.
     * @param player  The player to check.
     */
    public boolean canPay(RentRegion region, Player player) {

        double rentAmount = getRentCost(region);

        if (rentAmount == 0)
            return true;

        double playerBalance = Economy.getBalance(player.getUniqueId());
        return playerBalance >= rentAmount;
    }

    /**
     * Determine if the specified rent region can accept another payment
     * in advance.
     *
     * <p>Result is dependent on the maximum days in advance players are allowed
     * to pay.</p>
     *
     * @param region  The region to check.
     */
    public boolean canPayAdvance(RentRegion region) {

        Date expires = region.getExpirationDate();
        if (expires == null)
            throw new IllegalStateException("Expiration date is null.");

        long hours = DateUtils.getDeltaHours(new Date(), expires, TimeRound.ROUND_DOWN);

        int days = (int)Math.ceil((double)hours / 24);

        return days < _maxAdvanceDays + 1;
    }

    /**
     * Charge rent from the specified player for the specified region.
     *
     * @param region  The region to charge for.
     * @param player  The player to charge.
     *
     * @return  True if player was successfully charged.
     */
    public boolean charge(RentRegion region, Player player) {

        double rentAmount = getRentCost(region);

        if (rentAmount == 0)
            return true;

        if (!canPay(region, player))
            return false;

        Economy.withdraw(player.getUniqueId(), rentAmount);

        region.setPayed();

        RentPayedEvent.call(this, region, rentAmount);

        return true;
    }

    /**
     * Get the rent price for the specified region formatted
     * using the servers economy settings.
     *
     * @param region  The region.
     *
     * @return  The formatted price.
     */
    public String formatRentPrice(RentRegion region) {

        double rentAmount = getRentCost(region);

        return Economy.getCurrency().format(rentAmount);
    }

    // load bill collector settings
    private void load() {
        _costPerVolume = _dataNode.getDouble("cost-per-volume");
        _rentCycle = _dataNode.getInteger("rent-cycle");
        _maxAdvanceDays = _dataNode.getInteger("max-advance-days", _maxAdvanceDays);
    }

    /**
     * Repeating runnable (async) responsible for charging rent.
     */
    private class DueChecker implements Runnable {

        RentalSignHandler signHandler = RentalRooms.getSignHandler();
        Date nextUpdate = new Date();

        @Override
        public void run() {

            Date now = new Date();

            if (now.compareTo(nextUpdate) < 0)
                return;

            nextUpdate = DateUtils.addMinutes(now, 3);

            RentRegionManager manager = RentalRooms.getRegionManager();

            Collection<RentRegion> regions = manager.getAll();

            for (RentRegion region : regions) {
                if (!region.hasTenant())
                    continue;

                Date expires = region.getExpirationDate();
                if (expires == null)
                    continue;

                Tenant tenant = region.getTenant();

                // Check for eviction
                if (now.compareTo(expires) >= 0) { // overdue, evict

                    region.evict();

                    Msg.tellImportant(tenant.getPlayerID(), "rent_status_" + region.getName(), "You've been evicted from rent region '{0}'.", region.getName());

                    continue;
                }

                // Check for warning
                Date warningDate = DateUtils.addHours(now, -24);

                if (now.compareTo(warningDate) >= 0) {

                    Player p = tenant.getPlayer();
                    if (p != null) {

                        long hoursLeft = DateUtils.getDeltaHours(now, expires, TimeRound.ROUND_DOWN);

                        if (hoursLeft <= 1) {
                            long minutesLeft = DateUtils.getDeltaMinutes(now, expires, TimeRound.ROUND_DOWN);
                            Msg.tell(p, Lang.get(_RENT_DUE_MINUTES,
                                    formatRentPrice(region), region.getName(), minutesLeft));
                        }
                        else {
                            Msg.tell(p, Lang.get(_RENT_DUE_HOURS,
                                    formatRentPrice(region), region.getName(), hoursLeft));
                        }
                    }
                }

                signHandler.updateTimeLeft(region);
            }
        }
    }
}

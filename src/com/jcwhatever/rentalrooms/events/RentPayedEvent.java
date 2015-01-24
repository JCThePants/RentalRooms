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


package com.jcwhatever.rentalrooms.events;

import com.jcwhatever.rentalrooms.region.RentRegion;
import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.utils.PreCon;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;

/**
 * Called when a tenant pays rent.
 */
public class RentPayedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    /**
     * Call the event.
     *
     * @param source  The event caller.
     * @param region  The region that is receiving rent.
     * @param amount  The amount being payed.
     *
     * @return  The event after being called.
     */
    public static RentPayedEvent call(@Nullable Object source, RentRegion region, double amount) {
        PreCon.notNull(region);

        RentPayedEvent event = new RentPayedEvent(region, amount);
        return Nucleus.getEventManager().callBukkit(source, event);
    }

    private RentRegion _rentRegion;
    private double _amount;

    /**
     * Constructor.
     *
     * @param rentRegion  The rent region that is receiving payment.
     * @param amount      The amount being payed.
     */
    RentPayedEvent(RentRegion rentRegion, double amount) {
        _rentRegion = rentRegion;
        _amount = amount;
    }

    /**
     * Get the amount being payed.
     */
    public double getAmount() {
        return _amount;
    }

    /**
     * Get the rent region that is receiving rent payment.
     */
    public RentRegion getRentRegion() {
        return _rentRegion;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
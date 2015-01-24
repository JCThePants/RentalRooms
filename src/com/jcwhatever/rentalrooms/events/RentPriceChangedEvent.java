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

import com.jcwhatever.nucleus.Nucleus;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;

/**
 * Called when the price per unit of volume is changed.
 */
public class RentPriceChangedEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    /**
     * Call the event.
     *
     * @param source    The event caller.
     * @param oldPrice  The old price.
     * @param newPrice  The new price.
     *
     * @return  The event after being called.
     */
    public static RentPriceChangedEvent call(@Nullable Object source, double oldPrice, double newPrice) {

        RentPriceChangedEvent event = new RentPriceChangedEvent(oldPrice, newPrice);
        return Nucleus.getEventManager().callBukkit(source, event);
    }

    private final double _oldPrice;
    private double _newPrice;
    private boolean _isCancelled = false;

    /**
     * Constructor.
     *
     * @param oldAmount  The old rent amount.
     * @param newAmount  The new rent amount.
     */
    RentPriceChangedEvent(double oldAmount, double newAmount) {
        _oldPrice = oldAmount;
        _newPrice = newAmount;
    }

    /**
     * Get the old price per unit of volume.
     */
    public double getOldPrice() {
        return _oldPrice;
    }

    /**
     * Get the new price per unit of volume.
     */
    public double getNewPrice() {
        return _newPrice;
    }

    /**
     * Set the new price per unit of volume.
     *
     * @param price  The new price.
     */
    public void setNewPrice(double price) {
        _newPrice = price;
    }

    @Override
    public boolean isCancelled() {
        return _isCancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        _isCancelled = isCancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
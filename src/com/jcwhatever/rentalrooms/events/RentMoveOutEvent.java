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

import com.jcwhatever.rentalrooms.Tenant;
import com.jcwhatever.rentalrooms.region.RentRegion;
import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.utils.PreCon;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;

/**
 * Called when a tenant moves out of a region or is evicted.
 */
public class RentMoveOutEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    /**
     * Call the event.
     *
     * @param source  The event caller.
     * @param region  The region that is being moved out of.
     * @param tenant  The tenant that is moving out.
     *
     * @return  The event after being called.
     */
    public static RentMoveOutEvent call(@Nullable Object source, RentRegion region, Tenant tenant) {
        PreCon.notNull(region);
        PreCon.notNull(tenant);

        RentMoveOutEvent event = new RentMoveOutEvent(region, tenant);
        return Nucleus.getEventManager().callBukkit(source, event);
    }

    private final RentRegion _region;
    private final Tenant _oldTenant;
    private boolean _isCancelled = false;

    /**
     * Constructor
     *
     * @param region  The region that is being moved out of.
     * @param tenant  The tenant that is moving out.
     */
    RentMoveOutEvent(RentRegion region, Tenant tenant) {
        _region = region;
        _oldTenant = tenant;
    }

    /**
     * Get the rent region that is being moved out of.
     */
    public RentRegion getRentRegion() {
        return _region;
    }

    /**
     * Get the tenant that is moving out.
     */
    public Tenant getTenant() {
        return _oldTenant;
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

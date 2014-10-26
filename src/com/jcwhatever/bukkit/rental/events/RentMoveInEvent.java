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


package com.jcwhatever.bukkit.rental.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.jcwhatever.bukkit.rental.Tenant;
import com.jcwhatever.bukkit.rental.region.RentRegion;

public class RentMoveInEvent extends Event {
	
	private static final HandlerList _handlers = new HandlerList();
	
	private RentRegion _region;
	private Tenant _newTenant;
	private boolean _isCancelled = false;
	
	RentMoveInEvent(RentRegion region, Tenant newTenant) {
		_region = region;
		_newTenant = newTenant;
	}
	
	public RentRegion getRentRegion() {
		return _region;
	}
	
	public Tenant getNewTenant() {
		return _newTenant;
	}
	
	public boolean isCancelled() {
		return _isCancelled;
	}
	
	public void setIsCancelled(boolean isCancelled) {
		_isCancelled = isCancelled;
	}
	
	public HandlerList getHandlers() {
	    return _handlers;
	}
	 
	public static HandlerList getHandlerList() {
	    return _handlers;
	}
	
	public static RentMoveInEvent callEvent(RentRegion region, Tenant newTenant) {
		RentMoveInEvent event = new RentMoveInEvent(region, newTenant);
		
		if (hasListeners())
			Bukkit.getPluginManager().callEvent(event);
		
		return event;
	}

	public static boolean hasListeners() {
		return _handlers.getRegisteredListeners().length > 0;
	}
}

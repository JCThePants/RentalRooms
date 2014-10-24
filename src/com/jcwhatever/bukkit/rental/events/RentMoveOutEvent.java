package com.jcwhatever.bukkit.rental.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.jcwhatever.bukkit.rental.Tenant;
import com.jcwhatever.bukkit.rental.region.RentRegion;

public class RentMoveOutEvent extends Event {
	
	private static final HandlerList _handlers = new HandlerList();
	
	private RentRegion _region;
	private Tenant _oldTenant;
	private boolean _isCancelled = false;
	
	RentMoveOutEvent(RentRegion region, Tenant oldTenant) {
		_region = region;
		_oldTenant = oldTenant;
	}
	
	public RentRegion getRentRegion() {
		return _region;
	}
	
	public Tenant getOldTenant() {
		return _oldTenant;
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
	
	public static RentMoveOutEvent callEvent(RentRegion region, Tenant oldTenant) {
		RentMoveOutEvent event = new RentMoveOutEvent(region, oldTenant);
		
		if (hasListeners())
			Bukkit.getPluginManager().callEvent(event);
		
		return event;
	}

	public static boolean hasListeners() {
		return _handlers.getRegisteredListeners().length > 0;
	}
}

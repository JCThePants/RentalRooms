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

package com.jcwhatever.bukkit.rental.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RentPriceChangedEvent extends Event {
	
	private static final HandlerList _handlers = new HandlerList();
	
	private boolean _isCancelled = false;
	private double _oldAmount;
	private double _newAmount;
	
	RentPriceChangedEvent(double oldAmount, double newAmount) {
		_oldAmount = oldAmount;
		_newAmount = newAmount;
	}
	
	public double getOldAmount() {
		return _oldAmount;
	}
	
	public double getNewAmount() {
		return _newAmount;
	}
	
	public void setNewAmount(double amount) {
		_newAmount = amount;
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
	
	public static RentPriceChangedEvent callEvent(double oldAmount, double newAmount) {
		RentPriceChangedEvent event = new RentPriceChangedEvent(oldAmount, newAmount);
		
		if (hasListeners())
			Bukkit.getPluginManager().callEvent(event);
		
		return event;
	}

	public static boolean hasListeners() {
		return _handlers.getRegisteredListeners().length > 0;
	}
}
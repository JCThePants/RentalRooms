package com.jcwhatever.bukkit.rental.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.jcwhatever.bukkit.rental.region.RentRegion;


public class RentPayedEvent extends Event {
    
    private static final HandlerList _handlers = new HandlerList();
    
    private RentRegion _rentRegion;
    private double _amount;
    
    RentPayedEvent(RentRegion rentRegion, double amount) {
        _rentRegion = rentRegion;
        _amount = amount;
    }
    
    public double getAmount() {
        return _amount;
    }
    
    public RentRegion getRentRegion() {
        return _rentRegion;
    }
    
    public HandlerList getHandlers() {
        return _handlers;
    }
     
    public static HandlerList getHandlerList() {
        return _handlers;
    }
    
    public static RentPayedEvent callEvent(RentRegion region, double amount) {
        RentPayedEvent event = new RentPayedEvent(region, amount);
        
        if (hasListeners())
            Bukkit.getPluginManager().callEvent(event);
        
        return event;
    }

    public static boolean hasListeners() {
        return _handlers.getRegisteredListeners().length > 0;
    }
}
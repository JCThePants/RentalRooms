package com.jcwhatever.bukkit.rental.region;

import com.jcwhatever.bukkit.generic.economy.EconomyHelper;
import org.bukkit.entity.Player;

import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.rental.RentalRooms;

public class EconManager {

	
	private double _rentAmount;
	private int _rentCycle; // term units are minecraft days
	
	private boolean _isRentChanged = false;
	
	private long _lastPayment = 0;
	
	public EconManager(RentRegion region, IDataNode settings) {

	}
	
	public boolean isRentChanged() {
		return _isRentChanged;
	}
	
	public double getRentAmount() {
		return _rentAmount;
	}
	
	public int getRentCycle() {
		return _rentCycle;
	}
	
	public long getLastPayment() {
		return _lastPayment;
	}
	
	public boolean canPay(Player p) {
		if (_rentAmount == 0)
			return true;
		
		double playerBalance = EconomyHelper.getBalance(p);
		return playerBalance >= _rentAmount;
	}
	
	public String getFormattedRentAmount() {
		return EconomyHelper.formatAmount(_rentAmount);
	}
	
	public boolean charge(Player p) {
		if (_rentAmount == 0)
			return true;
		
		if (!canPay(p))
			return false;
		
		EconomyHelper.giveMoney(p, -(_rentAmount));
		return true;
	}

}

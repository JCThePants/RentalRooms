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


package com.jcwhatever.bukkit.rental.region;

import com.jcwhatever.nucleus.utils.EconomyUtils;
import com.jcwhatever.nucleus.storage.IDataNode;
import org.bukkit.entity.Player;

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
		
		double playerBalance = EconomyUtils.getBalance(p.getUniqueId());
		return playerBalance >= _rentAmount;
	}
	
	public String getFormattedRentAmount() {
		return EconomyUtils.formatAmount(_rentAmount);
	}
	
	public boolean charge(Player p) {
		if (_rentAmount == 0)
			return true;
		
		if (!canPay(p))
			return false;
		
		EconomyUtils.withdraw(p.getUniqueId(), _rentAmount);
		return true;
	}

}

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


package com.jcwhatever.bukkit.rental;

import com.jcwhatever.nucleus.utils.player.PlayerUtils;
import com.jcwhatever.nucleus.storage.IDataNode;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Friend {

	private UUID _playerId;
	private IDataNode _settings;
	private String _playerName;
		
	public Friend (UUID playerId, IDataNode settings) {
		_playerId = playerId;
		_settings = settings;

		loadSettings();
	}
	
	public UUID getPlayerId() {
		return _playerId;
	}
	
	public String getPlayerName() {
		if (_playerName == null) {
            _playerName = PlayerUtils.getPlayerName(_playerId);
            if (_playerName == null) {
                _playerName = "?";
            }
        }
		return _playerName;
	}
	
	public IDataNode getSettings() {
		return _settings;
	}
	
	private void loadSettings() {
		
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Friend) {
			return ((Friend)obj)._playerId.equals(_playerId);
		}
		else if (obj instanceof Player) {
			return ((Player)obj).getUniqueId().equals(_playerId);
		}
		else if (obj instanceof UUID) {
			return obj.equals(_playerId);
		}
		return false;
	}
	
}

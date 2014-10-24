package com.jcwhatever.bukkit.rental;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.jcwhatever.bukkit.generic.player.PlayerHelper;
import com.jcwhatever.bukkit.generic.storage.IDataNode;

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
		if (_playerName == null)
			_playerName = PlayerHelper.getPlayerName(_playerId);
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
			return ((UUID)obj).equals(_playerId);
		}
		return false;
	}
	
}

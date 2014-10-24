package com.jcwhatever.bukkit.rental.region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.rental.Friend;

public class FriendManager {

	private RentRegion _region;
	private IDataNode _settings;
	
	private Set<Friend> _friends;
	private Map<UUID, Friend> _friendMap;
	
	
	public FriendManager(RentRegion region, IDataNode settings) {
		_region = region;
		_settings = settings;
		_friends = new HashSet<Friend>();
		_friendMap = new HashMap<UUID, Friend>();
		
		loadSettings();
	}
	
	public RentRegion getRegion() {
		return _region;
	}
	
	void onEvict() {
		_friends.clear();
	}
	
	
	public void addFriend(Player p) {
		addFriend(p.getUniqueId());
	}
	
	public void addFriend(UUID friendId) {
		if (_friendMap.containsKey(friendId))
			return;
		
		IDataNode friendSettings = _settings.getNode(friendId.toString());
		Friend friend = new Friend(friendId, friendSettings);
		
		friendSettings.set("permissions", true);
		
		friendSettings.saveAsync(null);
		_friends.add(friend);
		_friendMap.put(friendId, friend);
	}
	
	public boolean hasFriend(Player p) {
		return hasFriend(p.getUniqueId());
	}
	
	public boolean hasFriend(UUID friendId) {
		return _friendMap.containsKey(friendId);
	}
	
	public void removeFriend(Player p) {
		removeFriend(p.getUniqueId());
	}
	
	public void removeFriend(UUID friendId) {
		Friend friend = _friendMap.remove(friendId);
		if (friend == null)
			return;
		
		friend.getSettings().clear();
		friend.getSettings().saveAsync(null);
		_friends.remove(friend);
	}
	
	public List<Friend> getFriends() {
		return new ArrayList<Friend>(_friends);
	}
	
	private void loadSettings() {
		Set<String> friendIds = _settings.getSubNodeNames();
		if (friendIds != null && !friendIds.isEmpty()) {
			
			for (String rawId : friendIds) {
				UUID friendId = UUID.fromString(rawId);
				if (friendId == null)
					continue;
				
				Friend friend = new Friend(friendId, _settings.getNode(rawId));
				
				_friends.add(friend);
				_friendMap.put(friendId, friend);
			}
			
		}
	}
	
}

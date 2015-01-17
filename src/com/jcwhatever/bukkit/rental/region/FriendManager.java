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

import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.bukkit.rental.Friend;
import com.jcwhatever.nucleus.utils.text.TextUtils;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

		friendSettings.save();
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
		friend.getSettings().save();
		_friends.remove(friend);
	}

	public List<Friend> getFriends() {
		return new ArrayList<Friend>(_friends);
	}

	private void loadSettings() {

		for (IDataNode node : _settings) {
			UUID friendId = TextUtils.parseUUID(node.getName());
			if (friendId == null)
				continue;

			Friend friend = new Friend(friendId, node);

			_friends.add(friend);
			_friendMap.put(friendId, friend);
		}
	}
}

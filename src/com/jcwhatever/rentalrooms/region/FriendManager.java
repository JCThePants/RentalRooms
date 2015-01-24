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


package com.jcwhatever.rentalrooms.region;

import com.jcwhatever.rentalrooms.Msg;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.text.TextUtils;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manage a {@link RentRegion} tenants friends.
 */
public class FriendManager {

    private final RentRegion _region;
    private final IDataNode _dataNode;
    private final Map<UUID, Friend> _friendMap = new HashMap<>(5);

    /**
     * Constructor.
     *
     * @param region    The owning rent region.
     * @param dataNode  The managers data node.
     */
    public FriendManager(RentRegion region, IDataNode dataNode) {
        _region = region;
        _dataNode = dataNode;

        load();
    }

    /**
     * Get the owning {@code RentRegion}.
     */
    public RentRegion getRegion() {
        return _region;
    }

    /**
     * Add a friend to the rent region.
     *
     * @param player  The player to add as a friend.
     *
     * @return  True if added, false if already added.
     */
    public boolean addFriend(Player player) {
        return addFriend(player.getUniqueId());
    }

    /**
     * Adda friend to the rent region.
     *
     * @param friendId  The ID of the player to add as a friend.
     *
     * @return  True if added, false if already added.
     */
    public boolean addFriend(UUID friendId) {
        if (_friendMap.containsKey(friendId))
            return false;

        IDataNode friendNode = _dataNode.getNode(friendId.toString());

        friendNode.set("permissions", true);
        friendNode.save();

        Friend friend = new Friend(friendId);
        _friendMap.put(friendId, friend);

        return true;
    }

    /**
     * Determine if the specified player is a friend
     * of the rent regions tenant.
     *
     * @param player  The player to check.
     */
    public boolean isFriend(Player player) {
        return isFriend(player.getUniqueId());
    }

    /**
     * Determine if the specified player is a friend
     * of the rent regions tenant.
     *
     * @param playerId  The ID of the player to check.
     */
    public boolean isFriend(UUID playerId) {
        return _friendMap.containsKey(playerId);
    }

    /**
     * Remove a friend from the rent region.
     *
     * @param player  The player to remove.
     *
     * @return  True if the friend was found and removed.
     */
    public boolean removeFriend(Player player) {
        return removeFriend(player.getUniqueId());
    }

    /**
     * Remove a friend from the rent region.
     *
     * @param playerId  The ID of the player to remove.
     * @return
     */
    public boolean removeFriend(UUID playerId) {
        Friend friend = _friendMap.remove(playerId);
        if (friend == null)
            return false;

        IDataNode friendNode = _dataNode.getNode(playerId.toString());
        friendNode.remove();
        friendNode.save();

        return true;
    }

    /**
     * Get the rent regions tenants friends.
     */
    public Collection<Friend> getFriends() {
        return Collections.unmodifiableCollection(_friendMap.values());
    }

    /**
     * Invoked by {@code RentRegion} when the tenant moves out or is evicted.
     */
    void onEvict() {
        _friendMap.clear();
    }

    private void load() {

        for (IDataNode node : _dataNode) {
            UUID friendId = TextUtils.parseUUID(node.getName());
            if (friendId == null) {
                Msg.debug("Invalid node name detected in friends data node. " +
                        "Should be player ID. Failed to parse ID: {0}", node.getName());
                continue;
            }

            Friend friend = new Friend(friendId);

            _friendMap.put(friendId, friend);
        }
    }
}

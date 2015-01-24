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


package com.jcwhatever.rentalrooms.commands.admin;

import com.jcwhatever.rentalrooms.Lang;
import com.jcwhatever.rentalrooms.Msg;
import com.jcwhatever.rentalrooms.RentalRooms;
import com.jcwhatever.rentalrooms.region.RentRegion;
import com.jcwhatever.rentalrooms.region.RentRegionManager;
import com.jcwhatever.nucleus.commands.AbstractCommand;
import com.jcwhatever.nucleus.commands.CommandInfo;
import com.jcwhatever.nucleus.commands.arguments.CommandArguments;
import com.jcwhatever.nucleus.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.messaging.ChatPaginator;
import com.jcwhatever.nucleus.utils.language.Localizable;
import com.jcwhatever.nucleus.utils.text.TextUtils.FormatTemplate;

import org.bukkit.command.CommandSender;

import java.util.Collection;

@CommandInfo(
        command="listall",
        staticParams={"page=1"},
        description="List all rental regions.",
        paramDescriptions = {
                "page= {PAGE}"
        })
public class ListAllCommand extends AbstractCommand {

    @Localizable static final String _PAGINATOR_TITLE = "Rental Regions";
    @Localizable static final String _LABEL_NO_TENANT = "<no tenant>";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws CommandException {

        int page = args.getInteger("page");

        RentRegionManager regionManager = RentalRooms.getRegionManager();

        Collection<RentRegion> regions = regionManager.getAll();

        ChatPaginator pagin = Msg.getPaginator(Lang.get(_PAGINATOR_TITLE));

        String noTenantLabel = Lang.get(_LABEL_NO_TENANT);
        for (RentRegion region : regions) {
            pagin.add(region.getName(), region.hasTenant()
                    ? region.getTenant().getPlayerName()
                    : noTenantLabel);
        }

        pagin.show(sender, page, FormatTemplate.LIST_ITEM);
    }
}

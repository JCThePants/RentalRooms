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


package com.jcwhatever.rentalrooms.commands.users;

import com.jcwhatever.nucleus.managed.commands.CommandInfo;
import com.jcwhatever.nucleus.managed.commands.arguments.ICommandArguments;
import com.jcwhatever.nucleus.managed.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.commands.mixins.IExecutableCommand;
import com.jcwhatever.nucleus.managed.commands.utils.AbstractCommand;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.nucleus.managed.messaging.ChatPaginator;
import com.jcwhatever.nucleus.utils.text.TextUtils.FormatTemplate;
import com.jcwhatever.rentalrooms.BillCollector;
import com.jcwhatever.rentalrooms.Lang;
import com.jcwhatever.rentalrooms.RentalRooms;
import com.jcwhatever.rentalrooms.Tenant;
import com.jcwhatever.rentalrooms.region.RentRegion;
import com.jcwhatever.rentalrooms.region.RentRegionManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.Collection;

@CommandInfo(
        command="info",
        staticParams={"rentalName", "page=1"},
        description="Get info about the specified rental region.",
        paramDescriptions = {
                "rentalName= The name of the rent region to get info about.",
                "page= {PAGE}"
        },
        permissionDefault=PermissionDefault.TRUE)

public class InfoCommand extends AbstractCommand implements IExecutableCommand {

    @Localizable static final String _RENTAL_NOT_FOUND = "Rental region '{0}' not found.";
    @Localizable static final String _PAGINATOR_TITLE = "Rent Info: {0}";
    @Localizable static final String _LABEL_NONE = "<none>";
    @Localizable static final String _LABEL_VACANT = "<vacant>";
    @Localizable static final String _LABEL_BLOCKS = "blocks";
    @Localizable static final String _LABEL_TENANT = "Tenant";
    @Localizable static final String _LABEL_PRICE = "Price";
    @Localizable static final String _LABEL_VOLUME = "Volume";
    @Localizable static final String _YOUR_RENTALS = "Your Rentals:";
    @Localizable static final String _DESCRIPTION = "{0} every {1} minecraft days";
    @Localizable static final String _STANDING_IN = "Standing in";
    @Localizable static final String _NO_REGION_HERE = "No rental region here.";

    @Override
    public void execute(CommandSender sender, ICommandArguments args) throws CommandException {

        String rentalName = args.getString("rentalName");
        int page = args.getInteger("page");

        RentRegionManager regionManager = RentalRooms.getRegionManager();
        BillCollector collector = RentalRooms.getBillCollector();

        RentRegion specifiedRegion = regionManager.get(rentalName);

        if (specifiedRegion == null)
            throw new CommandException(Lang.get(_RENTAL_NOT_FOUND, rentalName));

        ChatPaginator pagin = createPagin(args, 7, Lang.get(_PAGINATOR_TITLE, specifiedRegion.getName()));

        String noneLabel = Lang.get(_LABEL_NONE).toString();
        String vacantLabel = Lang.get(_LABEL_VACANT).toString();
        String blocksLabel = Lang.get(_LABEL_BLOCKS).toString();

        String label;

        label = Lang.get(_LABEL_TENANT).toString();
        pagin.add(label, specifiedRegion.hasTenant() ? specifiedRegion.getTenant().getPlayerName() : vacantLabel);

        label = Lang.get(_LABEL_PRICE).toString();
        pagin.add(label, collector.formatRentPrice(specifiedRegion));

        label = Lang.get(_LABEL_VOLUME).toString();
        pagin.add(label, specifiedRegion.getInteriorVolume() + " " + blocksLabel);


        if (sender instanceof Player) {

            Player p = (Player)sender;

            RentRegion standingRegion = regionManager.get(p.getLocation());
            pagin.add(Lang.get(_STANDING_IN), standingRegion != null ? standingRegion.getName() : Lang.get(_NO_REGION_HERE));

            pagin.addFormatted(FormatTemplate.RAW, ChatColor.GREEN + Lang.get(_YOUR_RENTALS).toString());

            Tenant tenant = Tenant.get(p);
            if (tenant == null) {
                pagin.addFormatted(FormatTemplate.RAW, noneLabel);
            }
            else {
                Collection<RentRegion> regions = tenant.getRentRegions();
                if (regions.size() == 0) {
                    pagin.addFormatted(FormatTemplate.RAW, noneLabel);
                }
                else {

                    String desc = Lang.get(_DESCRIPTION).toString();
                    for (RentRegion region : regions) {
                        pagin.add(region.getName(), desc, collector.formatRentPrice(region), collector.getRentCycle());
                    }
                }
            }
        }

        pagin.show(sender, page, FormatTemplate.CONSTANT_DEFINITION);
    }
}

package com.djrapitops.pluginbridge.plan.towny;

import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.utilities.html.HtmlStructure;
import com.djrapitops.plan.utilities.html.structure.AbstractAccordion;
import com.djrapitops.plan.utilities.html.structure.AccordionElement;
import com.djrapitops.plan.utilities.html.structure.AccordionElementContentBuilder;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility for creating Towny Accordion Html.
 *
 * @author Rsl1122
 */
public class TownsAccordion extends AbstractAccordion {

    private final List<Town> towns;
    private final PlayersMutator playersMutator;

    public TownsAccordion(List<Town> towns, PlayersMutator playersMutator) {
        super("towny_accordion");
        this.towns = towns;
        this.playersMutator = playersMutator;
    }

    private void addElements() {
        for (Town town : towns) {
            String townName = town.getName();
            Resident mayor = town.getMayor();
            String mayorName = mayor != null ? mayor.getName() : "NPC";

            String coordinates = "";
            try {
                Coord homeBlock = town.getHomeBlock().getCoord();
                coordinates = "x: " + homeBlock.getX() + " z: " + homeBlock.getZ();
            } catch (TownyException e) {
            }

            List<Resident> residents = town.getResidents();
            int residentCount = residents.size();
            String landCount = town.getPurchasedBlocks() + " / " + town.getTotalBlocks();

            Set<String> members = new HashSet<>();
            for (Resident resident : residents) {
                members.add(resident.getName());
            }

            PlayersMutator memberMutator = this.playersMutator.filterBy(
                    player -> player.getValue(PlayerKeys.NAME)
                            .map(members::contains).orElse(false)
            );

            SessionsMutator memberSessionsMutator = new SessionsMutator(memberMutator.getSessions());

            long playerKills = memberSessionsMutator.toPlayerKillCount();
            long mobKills = memberSessionsMutator.toMobKillCount();
            long deaths = memberSessionsMutator.toDeathCount();

            String separated = HtmlStructure.separateWithDots(("Residents: " + residentCount), mayorName);

            String htmlID = "town_" + townName.replace(" ", "-");

            String leftSide = new AccordionElementContentBuilder()
                    .addRowBold("brown", "user", "Major", mayorName)
                    .addRowBold("brown", "users", "Residents", residentCount)
                    .addRowBold("brown", "map", "Town Blocks", landCount)
                    .addRowBold("brown", "map-pin", "Location", coordinates)
                    .toHtml();

            String rightSide = new AccordionElementContentBuilder()
                    .addRowBold("red", "crosshairs", "Player Kills", playerKills)
                    .addRowBold("green", "crosshairs", "Mob Kills", mobKills)
                    .addRowBold("red", "frown-o", "Deaths", deaths)
                    .toHtml();

            addElement(new AccordionElement(htmlID, townName + "<span class=\"pull-right\">" + separated + "</span>")
                    .setColor("brown")
                    .setLeftSide(leftSide)
                    .setRightSide(rightSide));
        }
    }
}
package ch.frily.yubot.container;

import ch.frily.yubot.feature.ActiveModStatisticChart;
import ch.frily.yubot.feature.ActiveModTracking;
import ch.frily.yubot.interaction.select.select.ActiveModTrackingDetailSelect;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.entities.Member;

import java.time.YearMonth;
import java.util.*;
import java.util.List;

@Slf4j
public class ActiveModStatisticContainer extends Container {

    // TODO display all members, even when they have no active mod tracking

    private final Member initiator;

    public ActiveModStatisticContainer(Map<Member, List<ActiveModTracking>> activeModTrackingMap, Member initiator) {
        this.initiator = initiator;

        addTextDisplay("## Statistik der aktiven Moderatoren");

        activeModTrackingMap.forEach(this::addModSection);

        if (activeModTrackingMap.isEmpty()) {
            addTextDisplay("Es sind keine aktiven Moderatoren vorhanden.");
        }

        ActiveModTrackingDetailSelect detailSelect = new ActiveModTrackingDetailSelect();
        detailSelect.setActiveModTrackingMap(activeModTrackingMap);

        this.addComponent(MediaGallery.of(
                MediaGalleryItem.fromFile(new ActiveModStatisticChart().generateChart(activeModTrackingMap))
        ));

        this.addLineSeparator(Separator.Spacing.LARGE);

        this.addComponent(
                ActionRow.of(
                        detailSelect.build()
                )
        );
    }

    public void addModSection(Member member, List<ActiveModTracking> trackings){
        int totalActiveMinutes = trackings.stream().map(ActiveModTracking::activeTime).reduce(0, Integer::sum);
        ActiveModTracking currentMonthTracking = trackings.stream().filter(tracking -> Objects.equals(tracking.month(), YearMonth.now())).findFirst().orElse(null);
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        ActiveModTracking lastMonthTracking = trackings.stream().filter(tracking -> Objects.equals(tracking.month(), lastMonth)).findFirst().orElse(null);

        String thisMonth = "Diesen Monat: *Nicht registriert*";
        if (currentMonthTracking != null) {
            thisMonth = String.format("Diesen Monat: %s", Util.calcDuration(currentMonthTracking.activeTime()));
        }

        String tendency = "";
        if (currentMonthTracking != null && lastMonthTracking != null) {
            int difference = currentMonthTracking.activeTime() - lastMonthTracking.activeTime();
            if (difference > 0) {
                tendency = "📈";
            } else if (difference < 0){
                tendency = "📉";
            }
        }

        String youTag = member == initiator ? "*(Du)*" : "";

        String activeTag = Util.isActiveMod(member) ? "<:active1:1527044015927721984><:active2:1527044016942616748><:active3:1527044018276536403>" : "";

        addTextDisplay(String.format("""
                        ### [%s](https://discord.com/users/%s) %s %s
                        Total: %s
                        %s %s
                        -# ** **
                        """,
                member.getEffectiveName(), member.getId(), activeTag, youTag,
                Util.calcDuration(totalActiveMinutes),
                thisMonth, tendency)
        );
    }


}

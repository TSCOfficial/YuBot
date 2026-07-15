package ch.frily.yubot.container;

import ch.frily.yubot.feature.ActiveModTracking;
import ch.frily.yubot.interaction.select.select.ActiveModTrackingDetailSelect;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.SelectMenu;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.internal.entities.SelectMenuMentions;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ActiveModStatisticContainer extends Container {

    public ActiveModStatisticContainer(Map<Member, List<ActiveModTracking>> activeModTrackingMap, Member initiator) {
        addTextDisplay("## Statistik der aktiven Moderatoren");

        activeModTrackingMap.forEach(this::addModSection);

        if (activeModTrackingMap.isEmpty()) {
            addTextDisplay("Es sind keine aktiven Moderatoren vorhanden.");
        }

        ActiveModTrackingDetailSelect detailSelect = new ActiveModTrackingDetailSelect();
        detailSelect.setActiveModTrackingMap(activeModTrackingMap);

        this.addComponent(
                ActionRow.of(
                    detailSelect.build()
                )
        );
    }

    public void addModSection(Member member, List<ActiveModTracking> trackings) {
        int totalActiveMinutes = trackings.stream().map(ActiveModTracking::activeTime).reduce(0, Integer::sum);
        ActiveModTracking currentMonthTracking = trackings.stream().filter(tracking -> Objects.equals(tracking.month(), YearMonth.now())).findFirst().orElse(null);
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        ActiveModTracking lastMonthTracking = trackings.stream().filter(tracking -> Objects.equals(tracking.month(), lastMonth)).findFirst().orElse(null);

        String thisMonth = "Diesen Monat: *Nicht registriert*";
        if (currentMonthTracking != null) {
            thisMonth = String.format("Diesen Monat: %s", Util.calcDuration(currentMonthTracking.activeTime()));
        }

        String differenceLastMonth = "";
        if (lastMonthTracking != null) {
            int difference = currentMonthTracking.activeTime() - lastMonthTracking.activeTime();
            if (difference > 0) {
                differenceLastMonth = "📈";
            } else if (difference < 0){
                differenceLastMonth = "📉";
            }
        }

        addTextDisplay(String.format("""
                        ### %s - Total: %s
                        %s %s
                        -# ** **
                        """,
                member.getAsMention(), Util.calcDuration(totalActiveMinutes),
                thisMonth,
                differenceLastMonth)
        );
    }
}

package ch.frily.yubot.container;

import ch.frily.yubot.feature.ActiveModTracking;
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

    private Map<Member, List<ActiveModTracking>> activeModTrackingMap;

    public ActiveModStatisticContainer(List<ActiveModTracking> activeModTrackings, Member initiator) {
        addTextDisplay("## Statistik der aktiven Moderatoren");

        Map<Member, List<ActiveModTracking>> groupedActiveMods = new LinkedHashMap<>();

        activeModTrackings.forEach(activeModTracking -> {
            List<ActiveModTracking> groupedTrackings = groupedActiveMods.get(activeModTracking.moderator());
            if (groupedTrackings == null) {
                groupedTrackings = new ArrayList<>();
                groupedActiveMods.put(activeModTracking.moderator(), groupedTrackings);
            }
            groupedTrackings.add(activeModTracking);
            groupedActiveMods.replace(activeModTracking.moderator(), groupedTrackings);
        });

        // sort by total time
        activeModTrackingMap = groupedActiveMods.entrySet().stream()
                .sorted(Comparator.comparingInt(
                        (Map.Entry<Member, List<ActiveModTracking>> entry) ->
                                entry.getValue().stream().mapToInt(ActiveModTracking::activeTime).sum()
                ).reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        activeModTrackingMap.forEach(this::addModSection);

        if (activeModTrackingMap.isEmpty()) {
            addTextDisplay("Es sind keine aktiven Moderatoren vorhanden.");
        }

        this.addComponent(
                ActionRow.of(
                    StringSelectMenu.create("active-mod-tracking")
                        .setPlaceholder("Moderator auswählen")
                        .setMinValues(1)
                        .setMaxValues(1)
                        .addOptions(activeModTrackingMap.entrySet().stream()
                                .map(entry -> {
                                    SelectOption option = SelectOption.of(entry.getKey().getEffectiveName(), entry.getKey().getId());
                                    ActiveModTracking thisMonthsTracking = entry.getValue().stream().filter(tracking -> Objects.equals(tracking.month(), YearMonth.now())).findFirst().orElse(null);
                                    if (thisMonthsTracking != null) {
                                        option = option.withDescription(String.format("Dieser Monat: %s", Util.calcDuration(thisMonthsTracking.activeTime())));
                                    } else {
                                        option = option.withDescription("Dieser Monat: *Nicht registriert*");
                                    }
                                    return option;
                                })
                                .collect(Collectors.toList()))
                        .build()
                )
        );
        this.addTextDisplay("-# Selectmenu ist noch nicht implementiert.");
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

        String differenceLastMonth = "*Kein Vergleich Möglich*";
        if (currentMonthTracking != null && lastMonthTracking != null) {
            Month month = lastMonthTracking.month().getMonth();
            double differenceInPercent = (double) (currentMonthTracking.activeTime() - lastMonthTracking.activeTime()) / lastMonthTracking.activeTime() * 100;
            String icon = differenceInPercent > 0 ? "📈" : differenceInPercent < 0 ? "📉" : "📊";
            differenceLastMonth = String.format("Vergleich zum %s: %s %s", Util.translateMonth(month).toLowerCase(), icon, differenceInPercent + "%");
        }

        addTextDisplay(String.format("""
                        ### %s - Total: %s
                        %s
                        -# %s
                        ** **
                        """,
                member.getAsMention(), Util.calcDuration(totalActiveMinutes),
                thisMonth,
                differenceLastMonth)
        );

        // with buttons instead of dropdown:
        // this.addSection(
        //                Button.of(ButtonStyle.PRIMARY, "active-mod-tracking-view-profile-" + member.getIdLong(), "Profil anzeigen"),
        //                TextDisplay.ofFormat("""
        //                        **%s** - Total: %s
        //                        %s
        //                        %s
        //                        """,
        //                        member.getAsMention(), Util.calcDuration(totalActiveMinutes),
        //                        thisMonth,
        //                        differenceLastMonth)
        //        );

    }
}

package ch.frily.yubot.container;

import ch.frily.yubot.feature.ActiveModTracking;
import ch.frily.yubot.interaction.button.btn.ActiveModStatisticGoToHomeBtn;
import ch.frily.yubot.interaction.select.select.ActiveModTrackingDetailSelect;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Member;

import java.awt.*;
import java.time.Month;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class ActiveModStatisticDetailContainer extends Container {

    // TODO display all members, even when they have no active mod tracking

    /**
     *
     * @param member The member to show the details for
     * @param activeModTrackingMap All data to be able to provide differences to other members
     */
    public ActiveModStatisticDetailContainer(Member member, Map<Member, List<ActiveModTracking>> activeModTrackingMap) {
        List<ActiveModTracking> activeModTrackings = activeModTrackingMap.get(member);

        String activeTag = Util.isActiveMod(member) ? "<:active1:1527044015927721984><:active2:1527044016942616748><:active3:1527044018276536403>" : "";
        addFormatedText("# Statistik von %s %s", member.getEffectiveName(), activeTag);

        if (activeModTrackings.isEmpty()) {
            addTextDisplay("Keine Daten vorhanden.");
        } else {
            activeModTrackings.sort(Comparator.comparing(ActiveModTracking::month)); // sort by month
            int totalModActiveTime = activeModTrackings.stream().mapToInt(ActiveModTracking::activeTime).sum();
            addFormatedText("**Totale aktive Zeit:** %s", Util.calcDuration(totalModActiveTime));

            int totalActiveTimeOfAll = activeModTrackingMap.values().stream().flatMap(List::stream).mapToInt(ActiveModTracking::activeTime).sum();
            addFormatedText("**Totaler Beitrag:** %.0f%%", Util.calcPercentage(activeModTrackings.stream().mapToInt(ActiveModTracking::activeTime).sum(), totalActiveTimeOfAll));

            addLineSeparator(Separator.Spacing.LARGE);

            int index = 0;
            // this is used to be able to retrieve the correct data of the previous months while allowing a correct display of these data.
            // Both (view and data list) are january-december and get reversed at the end
            List<ContainerChildComponent> trackingViews = new java.util.ArrayList<>();
            for (ActiveModTracking activeModTracking : activeModTrackings) {
                if (index > 20) { // prevent container from being too long
                    break;
                }
                List<ActiveModTracking> thisMonthsActiveModTrackings = activeModTrackingMap.values().stream().flatMap(List::stream).toList()
                        .stream().filter(tracking -> tracking.month().equals(activeModTracking.month())).toList();


                int thisMonthsTotalActiveTime = thisMonthsActiveModTrackings.stream().mapToInt(ActiveModTracking::activeTime).sum();

                String differenceLastMonth = "";
                Month month = activeModTracking.month().getMonth();
                ActiveModTracking lastMonthTracking = activeModTrackings.stream().filter(tracking -> tracking.month().getMonth().equals(month.minus(1))).findFirst().orElse(null);
                if (lastMonthTracking != null) {
                    log.info("last month: {}, active time: {}", month.minus(1), lastMonthTracking.activeTime());
                    int difference = activeModTracking.activeTime() - lastMonthTracking.activeTime();
                    log.info("difference: {}", difference);
                    if (difference > 0) {
                        differenceLastMonth = String.format("Vergleich zum %s: +%s", Util.translateMonth(month).toLowerCase(), Util.calcDuration(difference));
                    } else {
                        differenceLastMonth = String.format("Vergleich zum %s: %s", Util.translateMonth(month).toLowerCase(), Util.calcDuration(difference));
                    }
                }



                StringBuilder monthDetail = new StringBuilder();
                monthDetail.append(String.format("### %s %d", Util.translateMonth(activeModTracking.month().getMonth()), activeModTracking.month().getYear())).append("\n");
                monthDetail.append(String.format("Aktive Zeit: %s", Util.calcDuration(activeModTracking.activeTime()))).append("\n");
                if (!differenceLastMonth.isEmpty()) {
                    monthDetail.append(String.format("-# *%s*", differenceLastMonth)).append("\n");
                }
                monthDetail.append(String.format("Beitrag: %.0f%%", Util.calcPercentage(activeModTracking.activeTime(), thisMonthsTotalActiveTime)));

                trackingViews.add(TextDisplay.of(monthDetail.toString()));

                index++;
            };

            addComponents(trackingViews.reversed());

            addInvisibleSeparator(Separator.Spacing.SMALL);
            addFormatedText("-# %d / %d Monate werden angezeigt", index, activeModTrackings.size());

        }


        addLineSeparator(Separator.Spacing.LARGE);

        ActiveModTrackingDetailSelect detailSelect = new ActiveModTrackingDetailSelect();
        detailSelect.setActiveModTrackingMap(activeModTrackingMap);
        detailSelect.setSelectedMember(member);

        this.addComponent(
                ActionRow.of(
                        detailSelect.build()
                )
        );
        this.addComponent(
                ActionRow.of(
                    new ActiveModStatisticGoToHomeBtn().build()
                )
        );
    }
}

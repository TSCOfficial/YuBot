package ch.frily.yubot.container;

import ch.frily.yubot.feature.ActiveModTracking;
import ch.frily.yubot.util.Util;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

public class ActiveModStatisticContainer extends Container {

    public ActiveModStatisticContainer(List<ActiveModTracking> activeModTrackingList, Member initiator) {
        addTextDisplay("## Statistik der aktiven Moderatoren");
        addTextDisplay("-# Hinweis: Die Statistik zählt die Zeit zwar bereits, sie ist aber momentan NICHT akkurat und kann um mehrere Minuten abweichen. Dazu wird es ein Refactoring (Update) geben.");

        activeModTrackingList.forEach(activeModTracking -> {
            if (activeModTracking.moderator().getIdLong() == initiator.getIdLong()) {
                addTextDisplay(String.format("%s (Du) - %s", activeModTracking.moderator().getAsMention(), Util.calcDuration(activeModTracking.activeTime())));
            }
            addTextDisplay(String.format("%s - %s", activeModTracking.moderator().getAsMention(), Util.calcDuration(activeModTracking.activeTime())));
        });
    }
}

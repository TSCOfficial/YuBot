package ch.frily.yubot.container.activemod;

import ch.frily.yubot.container.Container;
import ch.frily.yubot.container.ContainerContext;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.activemod.ActiveModStatisticChart;
import ch.frily.yubot.feature.activemod.ActiveModTracking;
import ch.frily.yubot.feature.activemod.ActiveModTrackingRepository;
import ch.frily.yubot.interaction.button.btn.activemod.ActiveModOptInBtn;
import ch.frily.yubot.interaction.button.btn.activemod.ActiveModOptOutBtn;
import ch.frily.yubot.interaction.button.btn.activemod.ActiveModShowStatisticBtn;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ActiveModDashboardContainer extends Container {

    public ActiveModDashboardContainer(ContainerContext context) {
        try {
            addTextDisplay("# Active Moderation");
            addTextDisplay("Siehe den aktuellen Status der Aktiven Moderator*innen und kontrolliere dein ActiveMod Status.");

            Role activeModRole = EnvResolver.getRoleById(EnvKey.ROLE_ACTIVEMOD);
            List<Member> activeMods = Util.getUsersByRole(activeModRole);
            addFormatedText("## Aktive Moderator*innen (%d)", activeMods.size());
            if (activeMods.size() > 0) {
                addFormatedText("%s\n%s", activeModRole.getAsMention(), activeMods.stream().map(Member::getEffectiveName).collect(Collectors.joining(", ")));
            } else {
                addFormatedText("%s\n*Keine aktiven Moderator*innen*", activeModRole.getAsMention());
            }

            // Add statistic
            Map<Member, List<ActiveModTracking>> activeModTrackingMap = ActiveModTrackingRepository.getActiveModTrackingsAsMap();
            activeModTrackingMap = ActiveModTrackingRepository.completeWithMissingModerators(activeModTrackingMap);
            this.addComponent(MediaGallery.of(
                    MediaGalleryItem.fromFile(new ActiveModStatisticChart().generateChart(activeModTrackingMap))
            ));


            addLineSeparator(Separator.Spacing.LARGE);
            addTextDisplay("-# Drückst du während deines Opt-in's auf Opt-in, wird der 30min Timer zurückgesetzt.");
            this.addComponent(
                    ActionRow.of(
                            new ActiveModOptInBtn().build(),
                            new ActiveModOptOutBtn().build(),
                            new ActiveModShowStatisticBtn().build()
                    )
            );
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

    }
}
